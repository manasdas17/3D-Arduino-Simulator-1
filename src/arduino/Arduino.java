package arduino;

import arduino.cpu.ATMega328;


public class Arduino {
	
	int x = 0; // Just For Debug
	
	int[] pins = new int[32]; // kac pin var hatirlamiyorum
	
	//byte[] Eeprom = new byte[1024];
	
	
	ATMega328 cpu = new ATMega328();
	
	public void step(int n) {
		
		//for(int i = 0; i < n; i++) {
		while (cpu.hasInstructions()) {
			System.out.println("\n[Arduino->run()] Iteration #" + x);
			if (cpu.hasInstructions()) cpu.execute();

			System.out.println(getCpuRegisters());
			System.out.println(getXYZPointers()); // + " " + getStackInfo());
			System.out.println(getCpuFlags() + "\n");


			x++;

			try{
				//do what you want to do before sleeping
				Thread.currentThread().sleep(1);//sleep for 1000 ms
				//do what you want to do after sleeptig
			}
			catch(Exception e){
				//If this thread was intrrupted by nother thread 
			}

		}
		
		
		/*
		for (int i = 0; i < cpu.noOfLoadedInstructions(); i++) {
			cpu.execute();
		}
		*/
		
	}

	public String getCpuRegisters() {
		String s = "";
		for (int i = 0; i < 17; i++) {
			s += "R" + i + ": " + (cpu.sram[i] & 0xff) + " ";
		}
		s += "\n";
		for (int i = 17; i < 32; i++) {
			s += "R" + i + ": " + (cpu.sram[i] & 0xff) + " ";
		}
		return s;
	}
	
	public String getCpuFlags() {
		//C Z N V S H T I  sreg 0x5f		
		String s = "C Z N V S H T I\n";		
		for (int i = 0; i < 8; i++)
			s += (BinaryFunctions.getBit(cpu.sram[0x5f], i)) ? "1 " : "0 ";
		return s;
	}
	
	public String getXYZPointers() {
		return "X: " + cpu.getXRegister() + " (0x" + Integer.toHexString(cpu.getXRegister()) + ") -> " + cpu.sram[cpu.getXRegister()]
				+ " Y: " + cpu.getYRegister() + " (0x" + Integer.toHexString(cpu.getYRegister()) + ") -> " + cpu.sram[cpu.getYRegister()]
				+ " Z: " + cpu.getZRegister() + " (0x" + Integer.toHexString(cpu.getZRegister()) + ") -> " + cpu.sram[cpu.getZRegister()];
	}
	
	public String getOffsetValue(String offset) {
		return "[SRAM " + offset + "] 0x" + Integer.toHexString(cpu.sram[Integer.parseInt(offset.substring(2), 16)] & 0xff);
	}
	
	/*
	public String getStackInfo() {
		
		return "SP: 0x" + Integer.toHexString(cpu.getStackPointer()) 
				+ " SPH: 0x" + Integer.toHexString((cpu.getStackPointer() >> 8) & 0x3)
				+ " SPL: 0x" + Integer.toHexString((cpu.getStackPointer() & 0xff))
				+ " STACK: 0x" + Integer.toHexString(cpu.getDataFromStack());
		
	}
	*/
}
