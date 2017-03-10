
// The file (structure) table is defined as follows:
public class FileTable {

	private Vector table;			// the actual entity of this file table
	private Directory directory;	// the root directory
	
	
	public FileTable(Directory directory) {	// constructor
		
		table = new Vector();			// instantiate a file (structure) table
		this.directory = directory;		// receive a reference to the Directory
										// from the file system
	}
	
	
	// major public methods
	public synchronized FileTableEntry falloc(String filename, String mode) {
		
		// allocate a new file (structure) table entry for this file name
		// allocate/receive and register the corresponding inode using directory
		// increment this inode's count
		// immediately write back this inode to the disk
		// return a reference to this file (structure) table entry

	}
	
	
	
	public synchronized boolean ffree(fileTableEntry e) {
		
		// receive a file table entry reference
		// save the corresponding inode to the disk
		// free this file table entry
		// return true if this file table entry found in my table
		
	}
	
	
	
	public synchronized boolean fempty() {
		
		return table.isEmpty();		// return if table is empty
									// should be called before starting a format
	}
	
}