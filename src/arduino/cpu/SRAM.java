package arduino.cpu;

public class SRAM {
	
	byte[] sram;
	
	public SRAM() {
		this.sram = new byte[64*1024];
	}
	
	public SRAM(int memsize) {
		this.sram = new byte[memsize];
	}
	
	public void writeByte(int offset, int data) {
		this.sram[offset & 0xffff] = (byte) (data & 0xff);		
	}
	
	public int readByte(int offset) {
		return this.sram[offset & 0xffff];
	}

}
