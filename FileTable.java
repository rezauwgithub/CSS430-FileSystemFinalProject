
import java.util.Vector;

// The file (structure) table is defined as follows:
public class FileTable {

	public final static int UNUSED = 0;
	public final static int USED = 1;
	
	public final static int READ = 2;
	public final static int WRITE = 3;
	

	private Vector<FileTableEntry> table;	// the actual entity of this file table
	private Directory directory;			// the root directory
	
	
	public FileTable(Directory directory) {	// constructor
		
		table = new Vector<FileTableEntry>();	// instantiate a file (structure) table
		this.directory = directory;				// receive a reference to the Directory
												// from the file system
	}
	
	
	// major public methods
	public synchronized FileTableEntry falloc(String filename, String mode) {
		
		// allocate a new file (structure) table entry for this file name
		// allocate/receive and register the corresponding inode using directory
		// increment this inode's count
		// immediately write back this inode to the disk
		// return a reference to this file (structure) table entry
		
		Inode inode = null;
		short iNumber = -1;
		
		while (true) {
			
			// Get the iNumber from the Inode for the filename provided to the function.
			if (filename.equals("/")) {
				iNumber = (short)0;
			}
			else {
				iNumber = directory.namei(filename);
			}
			
			
			if (iNumber >= 0) {
				inode = new Inode(iNumber);
				
				// Of r reading?
				if (mode.equals("r")) {
					
					// Flag is unused or used or reading (No read or write to file).
					if ((inode.flag == UNUSED) || (inode.flag == USED) || (inode.flag == READ)) {
						
						inode.flag = READ;
						break;
					}
					else if (inode.flag == WRITE) { // Written by someone else. Wait until finish.
						
						try {
							wait();
						}
						catch (InterruptedException ex) {
							
						}
						
					}
					
				}
				else { // Writing or Writing/Reading or Appending?
				
					// Flag of file used? If so, change to write
					if ((inode.flag == UNUSED) || (inode.flag == USED)) {
						
						inode.flag = WRITE;
						break;
					}
					else { // Flag is read or write? If so, then wait until finish.
					
						try {
							wait();
						} 
						catch (InterruptedException ex) {
							
						}
					}	
				}
				
				
			// If file does not have a node, create a new inode for it.
			// Using the alloc function from directory to get the iNumber	
			} 
			else if (!mode.equals("r")) {
				
				iNumber = directory.ialloc(filename);
				inode = new Inode(iNumber);
				inode.flag = WRITE;
				break;
			}
			else {
				
				return null;
			}
			
		}
		
		
		// Increment number of users count
		inode.count++;
		inode.toDisk(iNumber);
		
		// Create a new FileTableEntry and add it to the File Table
		FileTableEntry fileTableEntry = new FileTableEntry(inode, iNumber, mode);
		table.addElement(fileTableEntry);
		
		return fileTableEntry;
	}
	
	
	
	public synchronized boolean ffree(FileTableEntry fileTableEntry) {
		
		// receive a file table entry reference
		// save the corresponding inode to the disk
		// free this file table entry
		// return true if this file table entry found in my table
		
		Inode inode = new Inode(fileTableEntry.iNumber);
		
		if (table.remove(fileTableEntry)) {
			
			if (inode.flag == READ) {
				
				if (inode.count == 1) {
					
					// Free this file table entry
					notify();
					inode.flag = USED;
				}
			}
			else if (inode.flag == WRITE) {
				
				inode.flag = USED;
				
				notifyAll();
			}
			
			
			// Decrement the count of users
			inode.count--;
			
			// Save inode to the disk
			inode.toDisk(fileTableEntry.iNumber);
			
			
			return true;	
		}
		
		
		return false;
	}
	
	
	
	public synchronized boolean fempty() {
		
		return table.isEmpty();		// return if table is empty
									// should be called before starting a format
	}
	
}