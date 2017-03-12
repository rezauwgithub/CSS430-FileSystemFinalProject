

// The "/" root directory maintains each file in a different
// directory entry that contains its file name 
// (maximum 30 characters; 60 bytes in Java) and the corresponding
// inode number.
// The directory receives the maximum number of inodes to be created,
// (i.e., thus the max. number of files to be created) and keeps
// track of which inode numbers are in use. Since the directory
// itself is considered as a file, its contents are maintained by an
// inode, specifically inode 0. This can be located in the first
// 32 bytes of the disk block 1.

// Upon ThreadOS, the file system instantiates the Directory class
// as the root directory through its constructor,
// reads the file from the disk that can be found through the inode
// 0 at 32 bytes of the disk block 1, and initalizes the Directory
// instance with the file contents. Prior to shutdown, the file system
// must write back the Directory information onto the disk.
// The methods bytes2directory() and directory2bytes() will initialize
// the Directory instance with a byte array read from the disk and
// converts the Directory instance into a byte array that will be
// thereafter written back to the disk.
public class Directory {
	
	// max characters of each file name
	private final static int MAX_CHARS = 30;
	private final static int MAX_BYTES = 60;
	private final static int BYTES_ALLOCATED = 64;
	private final static int BLOCK = 4;
	
	private final static short ERROR = -1;
	
	
	// Directory entries
	
	// each element stores a different file size.
	private int fsize[];
	private char fnames[][];
	
	private int sizeOfDirectory;
	
	
	public Directory(int maxInumber) {	// directory constructor
		
		fsize = new int[maxInumber];	// maxInumber = max files
		
		for (int i = 0; i < maxInumber; i++) {
			fsize[i] = 0;	// all file size initialize to 0
		}
		
		fnames = new char[maxInumber][MAX_CHARS];
		
		sizeOfDirectory = maxInumber;
		
		String root = "/";			// entry(inode) 0 is "/"
		fsize[0] = root.length();	// fsize[0] is the size of "/"
		root.getChars(0, fsize[0], fnames[0], 0);	// fnames[0] includes "/"
		
	}
	
	
	public void bytes2directory(byte data[]) {
		
		// assumes data[] received directory information from disk
		// initializes the Directory instance with this data[]
		
		int offset = 0;
		for (int i = 0; i < sizeOfDirectory; i++) {
			
			fsize[i] = SysLib.bytes2int(data, offset);
			offset += BLOCK;
		}
		
		for (int i = 0; i < sizeOfDirectory; i++) {
			
			(new String(data, offset, MAX_BYTES)).getChars(0, fsize[i], fnames[i], 0);
			offset += MAX_BYTES;
		}
		
		
	}
	
	
	
	public byte[] directory2bytes() {
		
		// converts and return Directory information into a plain
		// byte array
		// this byte array will be written back to disk
		// note: only meaningful directory information should
		// be converted into bytes.
		
		byte[] directory = new byte[sizeOfDirectory * BYTES_ALLOCATED];
		int offset = 0;
		
		// Returns Directory information as a byte array
		for (int i = 0; i < sizeOfDirectory; i++) {
			
			SysLib.int2bytes(fsize[i], directory, offset);
			offset += BLOCK;
		}
		
		for (int i = 0; i < sizeOfDirectory; i++) {
			
			byte[] bytes = (new String(fnames[i], 0, fsize[i])).getBytes();
			
			System.arraycopy(bytes, 0, directory, offset, bytes.length);
			offset += MAX_BYTES;
		}
		
		
		return directory;
	}
	
	
	
	public short ialloc(String filename) {
		
		// filename is the one of a file to be created.
		// allocates a new inode number for this filename
		
		for (short i = 0; i < sizeOfDirectory; i++) {
			
			if (fsize[i] == 0) {
				
				if (filename.length() > MAX_CHARS) {
					fsize[i] = MAX_CHARS;
				}
				else {
					fsize[i] = filename.length();
				}
				
				
				filename.getChars(0, fsize[i], fnames[i], 0);
				
				return i;
			}
		}
		
		
		return ERROR;
	}
	
	
	
	public boolean ifree(short iNumber) {
		
		// deallocates this inumber (inode number)
		// the cooresponding file will be deleted.
		
		if ((iNumber < MAX_CHARS) && (fsize[iNumber] > 0)) {
			
			fsize[iNumber] = 0;
			return true;
		}
		else {
			
			return false;
		}
		
	}
	
	
	public short namei(String filename) {
		
		// returns the inumber corresponding to this filename
		
		for (short i = 0; i < sizeOfDirectory; i++) {
			
			if (filename.length() == fsize[i]) {
				
				if (filename.equals(new String(fnames[i], 0, fsize[i]))) {
					return i;
				}
			}
		}
		
		
		return ERROR;
	}
	
	
	
	// Used to cout the directory
	private void printDirectory() {
		
		for (int i = 0; i < sizeOfDirectory; i++) {
			
			SysLib.cout(i + ": " + fsize[i] + " bytes - ");
			
			for (int j = 0; j < MAX_CHARS; j++) {
				
				SysLib.cout(fnames[i][j] + " ");
			}
			
			
			SysLib.cout("\n");
		}
	}
	
	
	
	
}



















