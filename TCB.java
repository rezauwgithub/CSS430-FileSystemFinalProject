/**
*
* Implemented by Maryam Zare
* 
*/

// Each user thread maintains a user file descriptor table in its own TCB.
// Every time it opens a file, it allocates a new entry table including a
// reference to the corresponding file (structure) table entry. Whenever a thread
// spawns a new child thread, it passes a copy of its TCB to this child which
// thus has a copy of it's parent's user file descriptor table.
// This in turn means that both the parent and the child refer to the same file
// (structure) table entries and eventually share the same files.
public class TCB
 {
	private Thread thread = null;
	public static int MAX_ENTRY = 32;
    public static int ERROR = -1;
	private int tid = 0;
	private int pid = 0;
	private boolean terminate = false;
	
	// User file descriptor table:
	public FileTableEntry[] ftEnt = null;
	
	// each entry pointing to a file  table entry
	public TCB(Thread newThread, int myTid, int parentTid)
	{
		thread = newThread;
		tid = myTid;
		pid = parentTid;
		terminate = false;
		ftEnt = new FileTableEntry[32];//For file system		
	}
	
	public synchronized Thread getThread()
	 {
       return thread;
     }

    public synchronized int getTid()
	{
        return tid;
    }

    public synchronized int getPid()
	{
        return pid;
    }

    public synchronized boolean setTerminated()
	{
        terminate = true;
        return terminate;
    }

    public synchronized boolean getTerminated()
	{
        return terminate;
    }
	
	//
	public synchronized int getFd(FileTableEntry entry)
	{
        if (entry == null)
            return ERROR;
        for (int i = 3; i < MAX_ENTRY; i++)
		{
            if (ftEnt[i] == null)
			{
                ftEnt[i] = entry;
                return i;
            }
        }
        return ERROR;
    }
	
	
	public synchronized FileTableEntry returnFd(int fd)
	{

        if (fd >= 3 && fd < MAX_ENTRY){
            FileTableEntry fte = ftEnt[fd];
            ftEnt[fd] = null;
            return fte;
        }

        return null;
    }
	
	public synchronized FileTableEntry getFtEnt(int fd)
	{
        if (fd >= 3 && fd < MAX_ENTRY){
            return ftEnt[fd];
        }
        return null;
    }
}
	
