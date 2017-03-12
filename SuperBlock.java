

// The first disk block (block 0) is called the "superblock".
// It is used to describe:
// 		1. The number of disk blocks.
//		2. The number of inodes.
//		3. The block number of the head block of the free list.
//
// It is the OS-managed block. No other information must be
// recorded in and no user threads must be able to get access
// to the superblock.
public class SuperBlock {
	
	private final static int INVALID_BLOCK = -1;
	
	private final static int DEFAULT_BLOCKS = 1000;
	private final static int EMPTY_BLOCK = 0;
	private final static int DEFAULT_INODE_BLOCKS = 64;
	private final static int TOTAL_BLOCK_LOCATION = 0;
	private final static int TOTAL_INODE_LOCATION = 4;
	private final static int FREE_LIST_LOCATION = 8;
	
	
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
		if ((totalBlocks == diskSize) && (totalInodes > 0) && (freeList >= 2)) {
			return;	// Is valid
		}
		
		
		
		totalBlocks = diskSize;
		
		format(DEFAULT_INODE_BLOCKS);
		
	}
	
	
	// This function is used to erase blocks on the SuperBlock. It takes a byte array,
	// as well as a int representing the first block and another int representing the last block
	// we want to erase.
    private void eraseBlocksInRange(byte[] newEmptyBlock, int firstBlock, int lastBlock) {
		
        // Erase all blocks in given range (from firstBlock to lastBlock)
        for (int blockId = firstBlock; blockId < lastBlock; blockId++) {
            newEmptyBlock[blockId] = EMPTY_BLOCK;
        }
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
		
		
		byte[] newEmptyBlock = null;
		
		for (int i = freeList; i < DEFAULT_BLOCKS - 1; i++) {
			
			newEmptyBlock = new byte[Disk.blockSize];
			
			// Erase all disk blocks
			eraseBlocksInRange(newEmptyBlock, 0, Disk.blockSize);
			
			SysLib.int2bytes((i + 1), newEmptyBlock, 0);
			SysLib.rawwrite(i, newEmptyBlock);		
		}
		
		
		newEmptyBlock = new byte[Disk.blockSize];
		
		// Erase all disk blocks
		eraseBlocksInRange(newEmptyBlock, 0, Disk.blockSize);
		
		SysLib.int2bytes(-1, newEmptyBlock, 0);
		SysLib.rawwrite(DEFAULT_BLOCKS - 1, newEmptyBlock);
		
		byte[] superBlockReplacement = new byte[Disk.blockSize];
		
		
		// Copy all components back
		SysLib.int2bytes(totalBlocks, superBlockReplacement, TOTAL_BLOCK_LOCATION);
		SysLib.int2bytes(totalInodes, superBlockReplacement, TOTAL_INODE_LOCATION);
		SysLib.int2bytes(freeList, superBlockReplacement, FREE_LIST_LOCATION);
		
		// Write to superBlockReplacement
		SysLib.rawwrite(0, superBlockReplacement);
	}
	
	
	
	public void sync() {
		
		byte[] byteData = new byte[Disk.blockSize];
		
		SysLib.int2bytes(freeList, byteData, FREE_LIST_LOCATION);
		SysLib.int2bytes(totalBlocks, byteData, TOTAL_BLOCK_LOCATION);
		SysLib.int2bytes(totalInodes, byteData, TOTAL_INODE_LOCATION);
		
		SysLib.rawwrite(0, byteData);
	}
	
	
	
	
	
	public int nextFreeBlock() {
		
		if ((freeList > 0) && (freeList < totalBlocks)) {
			
			byte[] byteData = new byte[Disk.blockSize];
			
			SysLib.rawread(freeList, byteData);
			
					
			int blockLocation = freeList;
			
			// Update the next available (free) block.
			freeList = SysLib.bytes2int(byteData, 0);
			
			// return the blockLocation
			return blockLocation;
		}
		
		
		// If we get here, it means we our freeList state is invalid.
		return INVALID_BLOCK;
	}
	
	
	
	
	public boolean returnBlock(int blockId) {
		
		if ((blockId > 0) && (blockId < totalBlocks)) {
			
			int nextFreeBlock = freeList;
			
			int temp = 0;
			
			byte[] nextBlock = new byte[Disk.blockSize];
			byte[] newEmptyBlock = new byte[Disk.blockSize];
			
			eraseBlocksInRange(newEmptyBlock, 0, Disk.blockSize);
			
			SysLib.int2bytes(-1, newEmptyBlock, 0);
			
			
			while (nextFreeBlock != INVALID_BLOCK) {
				
				SysLib.rawread(nextFreeBlock, nextBlock);
				
				temp = SysLib.bytes2int(nextBlock, 0);
				if (temp == INVALID_BLOCK) {
					
					// Set the next free block
					SysLib.int2bytes(blockId, nextBlock, 0);
					SysLib.rawwrite(nextFreeBlock, nextBlock);
					SysLib.rawwrite(blockId, newEmptyBlock);
					
					
					// If we get here, that means our operation completed successfully. 
					return true;
				}
				
				
				nextFreeBlock = temp;
			}
		}
		
		return false;	// Returning invalid block.
	}
	
}