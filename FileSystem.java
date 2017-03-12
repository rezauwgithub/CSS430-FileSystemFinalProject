
/**
 * Implemnted by Maryam Zare
 */
//The file system class is performing all of the operations on disk. 
//It hides all of the implementation details from users.
//It provide a list of operations which users can directly use. 
//The class include functions of a file system.
//It has functions like format, open, write, read, delete, seek, and close. 
//The file system viewes an API for other files or users to run. 
// The file system instantiat the other classes that compose our solution.

public class FileSystem 
{
    private final static int SEEK_SET = 0;
    private final static int SEEK_CUR = 1;
    private final static int SEEK_END = 2;
	
	private final static boolean SUCCESS = true;
	private final static boolean FAIL = false;
    
	
    //Gives thumber partition blocks, 
	//size of partition blocks, Free block count and pointers
    private SuperBlock superblock;
    private Directory directory;
    private FileTable filetable;
	
	
// Creates an object superblock, directory, and file table. 
//and Stores file table in directory.
 public FileSystem (int blocks)
    {
    	superblock = new SuperBlock(blocks);
    	directory = new Directory(superblock.inodeBlocks);
    	filetable = new FileTable(directory);
    	
    	// read the "/" file from disk
    	FileTableEntry directoryEntry = open( "/", "r");
    	int directorySize = fsize( directoryEntry );
    	if ( directorySize > 0 )//already data in the directory
    	{
    	//read, and copy it to fsDirectory
    	 byte[] directoryData = new byte[directorySize];
    	 read( directoryEntry, directoryData );
    	 directory.bytes2directory(directoryData);
    	}
    	close
		( directoryEntry );
    }
//-------------------------sync------------------------------------------			
	//syncs the file system back to the physical disk.
    //The sync method will write the directory information 
	//to the disk form in the root directory. 
	//And ensure that the superblock is synced.
    public void sync()
    {
    	byte[] tempData = directory.directory2bytes();
    	//open root with write access
    	FileTableEntry root = open("/", "w");
        //write it to root
    	write(root, directory.directory2bytes());
        //close root 
    	close(root);
        //sync superblock
    	superblock.sync();
    }
//---------------------------format----------------------------------------	
    //The format method performs a full format of the disk,
    //and make sure all the contents of the disk and regenerating
	// the superblock, directory, and file tables. 
	//it is not reversible, once formatted all contents are lost.
	//we give the number of files (inodes) to be created by the superblock.
    public boolean format( int files)
	{
        //call format on superblock for arg number of files
    	superblock.format(files);
        //create directory, and register "/" in directory entry 0
    	directory = new Directory(superblock.inodeBlocks);
        //file table is created and store directory in the file table
    	filetable = new FileTable(directory);
        //return true on completion
        return SUCCESS;
    }
//---------------------------open----------------------------------------	
	 //This function open a file specified by filename and String passed.
     //Also String object pass another String object to present the mode 
	 //when it created, it checks to see if the mode which passed is a
	 //“w” for write. If it deletes all blocks and starts writing from scratch.
	 //then the new FileTableEntry object is returned to the calling function.
	
    public FileTableEntry open(String filename, String mode)
	{
    	FileTableEntry ftEntry = filetable.falloc(filename, mode);
    	if (mode == "w")
    	{
    		if ( !deallocAllBlocks( ftEntry ))
    		{
    			return null;
    		}
    	}
    	return ftEntry;//return FileTableEntry    
    }
//-------------------------------close------------------------------------	
	 //This function closes the file related to the given file table entry.
     //It returns true if successful false if not.
	
    public boolean close(FileTableEntry entry)
	{
    	//entry as synchronized
    	synchronized(entry) 
		{
			// decrease the number of users
			entry.count--;
			if (entry.count == 0) 
			{
				return filetable.ffree(entry);
			}
			return true;
		}
    }
//-------------------------------read------------------------------------	

	 //Read operation runs atomically. 
	 //Checks target block to make sure it is valid from to read, else breaks. 
	 //Then reads block and calculates the buffer based on data size.
	 //The amount of data read during each loop is determined by 
	 //the buffer size, and it gets read from the entry.
	 
	 //entry is index of file in process open-file table
	
    public int read(FileTableEntry entry, byte[] buffer)
	{
        //check write or append status
		if ((entry.mode == "w") || (entry.mode == "a"))
			return -1;
        int size  = buffer.length;//set total size of data to read
        int rBuffer = 0;//track data read
        int rError = -1;//track error on read
        int blockSize = 512;//set block size
        int itrSize = 0;//track how much is left to read

        //cast the entry as synchronized
        //loop to read bunch of data
        synchronized(entry)
        {
        	while (entry.seekPtr < fsize(entry) && (size > 0))
        	{
        		int currentBlock = entry.inode.findTargetBlock(entry.seekPtr);
        		if (currentBlock == rError)
        		{
        			break;
        		}
				byte[] data = new byte[blockSize];
        		SysLib.rawread(currentBlock, data);
        		
        		int dataOffset = entry.seekPtr % blockSize;
        		int blocksLeft = blockSize - itrSize;
        		int fileLeft = fsize(entry) - entry.seekPtr;
        		
        		if (blocksLeft < fileLeft)
					itrSize = blocksLeft;
				else
				    itrSize = fileLeft;

				if (itrSize > size)
					itrSize = size;

        		System.arraycopy(data, dataOffset, buffer, rBuffer, itrSize);
        		rBuffer += itrSize;
        		entry.seekPtr += itrSize;
        		size -= itrSize;
        	}
        	return rBuffer;
        }
    }   
//-------------------------------read------------------------------------	
	 //Writes the contents of buffer to the file indicated by entry, 
	 //starting at the position indicated by the seek pointer.
	 //Increments the seek pointer by the number of bytes that wrote. 
	 //The return value is the number of bytes which wrote, or -1.
	 
    public int write(FileTableEntry entry, byte[] buffer){
    	int bytesWritten = 0;
		int bufferSize = buffer.length;
		int blockSize = 512;
		
		if (entry == null || entry.mode == "r")
		{
			return -1;
		}

		synchronized (entry)
		{
			while (bufferSize > 0)
			{
			 int location = entry.inode.findTargetBlock(entry.seekPtr);
			 
				if (location == -1)// if current block null
				{
				 short newLocation = (short) superblock.nextFreeBlock();
				 int testPtr = entry.inode.getIndexBlockNumber(entry.seekPtr, newLocation);

					if (testPtr == -3)
					{
					 short freeBlock = (short) this.superblock.nextFreeBlock();
						// indirect pointer is empty
						if (!entry.inode.setIndexBlock(freeBlock))
						{
							return -1;
						}
						// check block pointer error
						if (entry.inode.getIndexBlockNumber(entry.seekPtr, newLocation) != 0)
						{
							return -1;
						}
					}
					else if (testPtr == -2 || testPtr == -1)
					{
						return -1;
					}
					
					location = newLocation;
				}

				byte [] tempBuff = new byte[blockSize];
				SysLib.rawread(location, tempBuff);

				int tempPtr = entry.seekPtr % blockSize;
				int diff = blockSize - tempPtr;

				if (diff > bufferSize)
				{
					System.arraycopy(buffer, bytesWritten, tempBuff, tempPtr, bufferSize);
					SysLib.rawwrite(location, tempBuff);

					entry.seekPtr += bufferSize;
					bytesWritten += bufferSize;
					bufferSize = 0;
				}
				else {
					System.arraycopy(buffer, bytesWritten, tempBuff, tempPtr, diff);
					SysLib.rawwrite(location, tempBuff);

					entry.seekPtr += diff;
					bytesWritten += diff;
					bufferSize -= diff;
				}
			}
			// update inode length if seekPtr larger
			if (entry.seekPtr > entry.inode.length)
			{
				entry.inode.length = entry.seekPtr;
			}
			entry.inode.toDisk(entry.iNumber);
			return bytesWritten;
		}
    }
//-------------------------------seek------------------------------------	

	 //This function updates the seek pointer corresponding to a given file table entry. It returns 0 if the update was
	 //successful, -1 otherwise. In the case that the user attempts to set the seek pointer to a negative number, the
	 //method will set it to 0. In the case that the user wants to set the pointer beyond the file size the method sets
	 //the seek pointer to the end of the file. In both cases the method returns that the operation was performed
	 //successfully.
	
    public int seek(FileTableEntry entry, int offset, int location)
	{  	
    	synchronized (entry)
		{
			switch(location)
			{
				//beginning of file
				case SEEK_SET:
					//set seek pointer to offset of beginning of file
					entry.seekPtr = offset;
					break;
				// current position
				case SEEK_CUR:
					entry.seekPtr += offset;
					break;
				// if from end of file
				case SEEK_END:
					// set seek pointer to size + offset
					entry.seekPtr = entry.inode.length + offset;
					break;
				// unsuccessful
				default:
					return -1;
			}
			if (entry.seekPtr < 0)
			  {
				entry.seekPtr = 0;
			  }

			if (entry.seekPtr > entry.inode.length)
			{
				entry.seekPtr = entry.inode.length;
			}

			return entry.seekPtr;
		}
    }
//----------------------------deallocAllBlocks-----------------------------------	

	//Checks if inodes blocks are valid, else error.
	//go through all the direct pointer blocks and calls superblock to return if valid. 
	//It then handles indirect pointer from inode and calls returnBlock().
	//It finishes by writing back inodes to disk.
	
    private boolean deallocAllBlocks(FileTableEntry fileTableEntry)
	{
     short invalid = -1;
	 
    	if (fileTableEntry.inode.count != 1)
		{
			SysLib.cerr("Null Pointer");
			return false;
		}

		for (short blockId = 0; blockId < fileTableEntry.inode.DIRECT_SIZE; blockId++)
		{
			if (fileTableEntry.inode.direct[blockId] != invalid)
			{
				superblock.returnBlock(blockId);
				fileTableEntry.inode.direct[blockId] = invalid;
			}
		}

		byte [] data = fileTableEntry.inode.freeIndirectBlock();

		if (data != null)
		{
				short blockId;
			while((blockId = SysLib.bytes2short(data, 0)) != invalid)
			{
				superblock.returnBlock(blockId);
			}
		}
		fileTableEntry.inode.toDisk(fileTableEntry.iNumber);
		return true;
    }
//----------------------------delete-----------------------------------	
	
  //This function delet a specified file by the filename string  passed in.
  //It begins by opening and creating a temporary FileTableEntry object to contain the iNode(TCB)object. 
  //This allows to have access to all private members of this filename entry. 
  //With this iNode, we use it’s iNumber to free it up from Directory’s tables.
  //then close the FileTableEntry object using the close()function.
  //As long as both the free() and close() are successful,we return true.
  //Otherwise we return false
	 
	boolean delete(String filename)
	{
		FileTableEntry tcb = open(filename, "w");//Grab the TCB (iNode)
		if (directory.ifree(tcb.iNumber) && close(tcb)) 
		{ 
	      //try to free and delete
			return SUCCESS; //Delete was completed
		} 
		else 
		{
			return FAIL; //Was not last open
		}
	}
//----------------------------fsize-----------------------------------	

	 //Returns the file size in bytes atomically.
	
    public synchronized int fsize(FileTableEntry entry){
        //cast the entry as synchronized
    	synchronized(entry)
    	{
	        // Set a new Inode object to the entries Inode
			Inode inode = entry.inode;
	        // return the length on the new Inode object
    		return inode.length;
    	}
    }
}