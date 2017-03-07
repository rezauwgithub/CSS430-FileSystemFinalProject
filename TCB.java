

// Each user thread maintains a user file descriptor table in its own TCB.
// Every time it opens a file, it allocates a new entry table including a
// reference to the corresponding file (structure) table entry. Whenever a thread
// spawns a new child thread, it passes a copy of its TCB to this child which
// thus has a copy of it's parent's user file descriptor table.
// This in turn means that both the parent and the child refer to the same file
// (structure) table entries and eventually share the same files.
public class TCB {

	private Thread thread = null;
	private int tid = 0;
	private int pid = 0;
	private boolean terminate = false;
	
	// User file descriptor table:
	// each entry pointing to a file (structure) table entry
	public FileTableEntry[] ftEnt = null;
	
	
	public TCB(Thread newThread, int myTid, int parentTid) {
		
		thread = newThread;
		tid = myTid;
		pid = parentTid;
		terminated = false;
		
		// The following code is added for the file system
		ftEnt = new FileTableEntry[32];
		for (int i = 0; i < 32; i++) {
			
			ftEnt[i] = null;		// all entries initialized to null
			// fd[0], fd[1], and fd[2] are kept null.
		}
		
		
	}
	
	
}