

// The first disk block, block 0, is called the "superblock".
// It is used to describe:
// 		1. The number of disk blocks.
//		2. The number of inodes.
//		3. The block number of the head block of the free list.
//
// It is the OS-managed block. No other information must be
// recorded in and no user threads must be able to get access
// to the superblock.
public class SuperBlock {
	
	private static final int DEFAULT_INODE_BLOCKS = 64;
	private static final int TOTAL_BLOCK_LOCATION = 0;
	private static final int TOTAL_INODE_LOCATION = 4;
	private static final int FREE_LIST_LOCATION = 8;
	private static final int DEFAULT_BLOCKS = 1000;
	
	
	public int totalBlocks;		// the number of disk blocks
	public int totalInodes;		// the number of inodes;
	public int freeList;		// the block number of the free list's head
	
	public int inodeBlocks;
	
	
	public SuperBlock(int diskSize) {
		
		byte[] superBlock = new byte[Disk.blockSize];
		
		SysLib.rawread(0, superBlock);
		
		totalBlocks = SysLib.bytes2int(superBlock, TOTAL_BLOCK_LOCATION);
		totalInodes = SysLib.bytes2int(superBlock, TOTAL_INODE_LOCATION);
		freeList = SysLib.bytes2int(superBlock, FREE_LIST_LOCATION);
		
		inodeBlocks = totalInodes;
		
		
		// Validate disk contents
		if ((totalBlock == diskSize) && (totalInodes > 0) && (freeList >= 2)) {
			return;	// Is valid
		}
		
		
		
		totalBlocks = diskSize;
		
	}
	
	
	
	public void format(int fileCount) {
		
		if (fileCount < 0) {
			fileCount = DEFAULT_INODE_BLOCKS;
		}
		
		totalInodes = fileCount;
		inodeBlocks = totalInodes;
		
		
		Inode newInode = null;
		
		for (int i = 0; i < totalInodes; i++) {
			
			newInode = new Inode();
			newInode.flag = 0;
			newInode.toDisk((short)i);	
		}
		
		
		// Note: 16 Inodes can be stored in one block.
		freeList = ((totalInodes / 16) + 2);
		
		byte[] newBlock =
		
		
		
		
		
	}
	
}