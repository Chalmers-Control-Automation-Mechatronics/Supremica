package org.supremica.util.BDD;

/**
 * A ring queue of integers. has limited capacity, and if exceeded, data will be overwritten
 *
 */
public class IntQueue
{
	private int size, read, write;
	private int[] data;

	public IntQueue(int size)
	{
		size++;    // otherwise, a completely full queue would look empty!!

		this.size = size;
		this.read = 0;
		this.write = 0;
		this.data = new int[size];
	}

	public void enqueue(int x)
	{
		data[write] = x;
		write = (write + 1) % size;
	}

	public int dequeue()
	{
		int ret = data[read];

		read = (read + 1) % size;

		return ret;
	}

	public boolean empty()
	{
		return (read == write);
	}

	public void reset()
	{
		read = write = 0;
	}

/*
		public static void main(String [] args) {
				IntQueue q = new IntQueue (5);

				q.enqueue(0);
				q.enqueue(2);
				q.enqueue(3);
				q.enqueue(4);

				while(!q.empty()) {
						System.out.println("-->" + q.dequeue() );
				}
		}
		*/
}
