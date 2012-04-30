package arduino.cpu;

public class SRAM {
	
	byte[] sram;
	
	public SRAM() {		// just for maintenance
		this.sram = new byte[64*1024];
	}
	
	public SRAM(int memsize) {
		this.sram = new byte[memsize];
	}
	
	//henuz islevi yok bos klas ama tek array yerine bunu yazsam
	// cok iyi olacak yoksa problem cikabilir
	
	// input/output
	public void writeByte(int offset, int data) {
		this.sram[offset & 0xffff] = (byte) (data & 0xff);		
	}
	
	/*
	public void writeByte(int offset, byte data) {
		this.sram[offset] = (byte) (data & 0xff);		
	}
	*/
	
	/*
	public int readByte(byte offset) {
		return this.sram[offset] & 0xff;
	}
	*/
	
	public int readByte(int offset) {
		return this.sram[offset & 0xffff];
	}

}
