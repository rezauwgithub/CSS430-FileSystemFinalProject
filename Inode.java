

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
	private final static int MAX_BYTES = 512;
	private final static int BLOCK_SIZE = 16;
	private final static int INT_BLOCK = 4;
	private final static int SHORT_BLOCK = 2;
	private final static int ERROR = -1;
	
	
	public final static int DIRECT_SIZE = 11;	// # direct pointers
	
	
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
		
		// Calculate the total number of blocks to read
		int numberOfBlocks = 1 + (iNumber / BLOCK_SIZE);
		
		// Allocate byteData
		byte[] byteData = new byte[MAX_BYTES];
		SysLib.rawread(numberOfBlocks, byteData);
		
		// Define the offset
		int offset = ((iNumber % BLOCK_SIZE) * INODE_SIZE);
		
		
		// Create the space
		length = SysLib.bytes2int(byteData, offset);
		offset += INT_BLOCK;
		
		count = SysLib.bytes2short(byteData, offset);
		offset += SHORT_BLOCK;
		
		flag = SysLib.bytes2short(byteData, offset);
		offset += SHORT_BLOCK;
		
		
		for (int i = 0; i < DIRECT_SIZE; i++) {
			
			direct[i] = SysLib.bytes2short(byteData, offset);
			offset += SHORT_BLOCK;
		}
		
		indirect = SysLib.bytes2short(byteData, offset);
		offset += SHORT_BLOCK;
		
		
	}
	
	
	int toDisk(short iNumber) {
		// design it by yourself
		
		byte[] byteData = new byte[INODE_SIZE];
		
		int offset = 0;
		
		SysLib.int2bytes(length, byteData, offset);
		offset += INT_BLOCK;
		
		SysLib.short2bytes(count, byteData, offset);
		offset += SHORT_BLOCK;
		
		SysLib.short2bytes(flag, byteData, offset);
		offset += SHORT_BLOCK;
		
		for (int i = 0; i < DIRECT_SIZE; i++) {
			
			SysLib.short2bytes(direct[i], byteData, offset);
			offset += SHORT_BLOCK;
		}
		
		SysLib.short2bytes(indirect, byteData, offset);
		offset += SHORT_BLOCK;
		
		int numberOfBlocks = 1 + (iNumber / BLOCK_SIZE);
		byte[] newByteData = new byte[MAX_BYTES];
		SysLib.rawread(numberOfBlocks, newByteData);
		
		offset = ((iNumber % BLOCK_SIZE) * INODE_SIZE);
		
		System.arraycopy(byteData, 0, newByteData, offset, INODE_SIZE);
		SysLib.rawwrite(numberOfBlocks, newByteData);
		
		
		
		return 0;
	}
	
	
	int getIndexBlockNumber(int entry, short offset) {
		
		int toFind = (entry / MAX_BYTES);
		if (toFind < DIRECT_SIZE) {
			
			if (direct[toFind] >= 0) {
				
				return -1;
			}
			
			
			if ((toFind > 0) && (direct[toFind - 1] == -1)) {
				
				return -2;
			}
			
			
			direct[toFind] = offset;
			
			return 0;
		}
		
		
		if (indirect < 0) {
				
			return -3;
		}
		
		
		byte[] byteData = new byte[MAX_BYTES];
		
		SysLib.rawread(indirect, byteData);
		
		int blockSpace = 2 * (toFind - DIRECT_SIZE);
		if (SysLib.bytes2short(byteData, blockSpace) > 0) {
			
			return ERROR;
		}
		
		
		SysLib.short2bytes(offset, byteData, blockSpace);
		SysLib.rawwrite(indirect, byteData);
		
		
		return 0;
	}
	
	
	
	boolean setIndexBlock(short blockIndex) {
		
		for (int i = 0; i < DIRECT_SIZE; i++) {
			
			if(direct[i] == ERROR) {
				
				return false;
			}
		}
		
		
		if (indirect != ERROR) {
			
			return false;
		}
		
		
		indirect = blockIndex;
		byte[] byteData = new byte[MAX_BYTES];
		
		for (int i = 0; i < (MAX_BYTES / 2); i++) {
			SysLib.short2bytes((short)ERROR, byteData, (2 * i));
		}
		
		SysLib.rawwrite(blockIndex, byteData);
		
		
		return true;
	}
	
	
	
	int findTargetBlock(int offset) {
	
		int toFind = (offset / MAX_BYTES);
		if (toFind < DIRECT_SIZE) {
			
			return direct[toFind];
		}
		
		if (indirect < 0) {
			
			return -1;
		}
		
		
		byte[] byteData = new byte[MAX_BYTES];
		SysLib.rawread(indirect, byteData);
		
		
		return SysLib.bytes2short(byteData, (2 * (toFind - DIRECT_SIZE)));
	}
	
	
	
	byte[] freeIndirectBlock() {
		
		if (indirect >= 0) {
			
			byte[] byteData = new byte[MAX_BYTES];
			SysLib.rawread(indirect, byteData);
			
			indirect = -1;
			
			return byteData;
		}
		
		
		// If we get here, return NULL
		return null;
	}
	
	
	
	
	
}