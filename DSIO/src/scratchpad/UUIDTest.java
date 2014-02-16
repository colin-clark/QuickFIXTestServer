package scratchpad;

import com.eaio.uuid.UUID;

public class UUIDTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		UUID u;
		
		for(int i=0;i<2000;i++) {
			u = new UUID();
			System.out.println("UUID ClockSeq&Node:"+u.clockSeqAndNode);
			System.out.println("UUID String       :"+u.toString());
			System.out.println(System.nanoTime());
			System.out.println(System.currentTimeMillis());
			System.out.println("---");
		}
		// all done
		System.exit(0);
	}

}
