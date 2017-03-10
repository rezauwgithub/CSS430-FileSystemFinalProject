

// The file system maintains the file (structure) table shared
// among all user threads. When a user thread opens a file,
// it follows the sequence listed below:
//		1. The user thread allocates a new entry of the user
//			file descriptor table in its TCB. This entry number
//			itself becomes a file descriptor number. The entry
//			maintains a reference to a file (structure) table entry.
//
//		2. The user thread then requests the file system to allocate
//			a new entry of the system-maintained file (structure)
//			table. This entry includes the seek pointer of this file,
//			a reference to the inode corresponding to the file,
//			the inode number, the count to maintain #threads sharing
//			this file (structure) table, and the access mode.
//			The seek pointer is set to the front or the tail of this
//			file depending on the file access mode.

//		3. The file system locates the corresponding inode and records
//			it in this file (structure) table entry.
//
//		4. The user thread finally registers a reference to this
//			file (structure) table entry in its file descriptor
//			table entry of the TCB.
//
//	The file (structure) table entry should be:
public class FileTableEntry {	// Each table entry should have

	public int seekPtr;			// 		a file seek pointer
	public final Inode inode;	//		a reference to its inode
	public final short iNumber;	//		this inode number
	public int count;			//		# threads sharing this entry
	public final String mode;	//		"r", "w", "w+", or "a"
	
	
	public FileTableEntry(Inode inode, short iNumber, String mode) {
		
		seekPtr = 0;		// the seek pointer is set to the file top
		this.inode = i;
		this.iNumber = iNumber;
		count = 1;			// at least one thread is using this entry
		this.mode = mode;
		if (this.mode.compareTo("a") == 0) {	// if mode is appended,
			seekPtr = inode.length;				// seekPtr points to the end of file.
		}
	}	
}

