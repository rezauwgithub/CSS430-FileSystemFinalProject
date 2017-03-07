

// Starting from teh blocks after the superblock will be
// the inode blocks. Each inode describes one file.
// Our inode is a simplified version of the Unix inode.
// It includes 12 pointers of the index block.
// The first 11 of these pointers point to direct blocks.
// The last pointer pointers to an indirect block.
// In addition, each inode must include
// 		(1) the length of the corresponding file,
//		(2) the number of file (structure) table entries that
//			point to this inode, and
//		(3) the flag to indicate if it is unused (= 0), used (= 1),
//			or in some other status (= 2, 3, 4, ...).
//			16 inodes can be stored in one block.
// 			
public class Inode {
	
	private final static int INODE_SIZE = 32;	// fix to 32 bytes
	private final static int DIRECT_SIZE = 11;	// # direct pointers
	
	public int length;		// file size in bytes
	public short count;		// # file-table entries pointing to this
	public short flag;		// 0 = unused, 1 = used, ...
	public short direct[] = new short[DIRECT_SIZE];		// direct pointers
	public short indirect;	// a new indirect pointer
	
	
	Inode() {		// a default contructor
		
		length = 0;
		count = 0;
		flag = 1;
		
		for (int i = 0; i < DIRECT_SIZE; i++) {
			direct[i] = -1;
		}
		
		indirect = -1;	
	}
	
	
	Inode(short iNumber) {			// retrieving inode from disk
		// design it by yourself.
	}
	
	
	int toDisk(short iNumber) {
		// design it by yourself
	}
	
}