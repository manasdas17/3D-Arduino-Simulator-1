package arduino;

import arduino.cpu.ATmega328P;


public class Arduino {

	int x = 0; // Just For Debug

	int[] pins = new int[32]; // kac pin var hatirlamiyorum

	//byte[] Eeprom = new byte[1024];


	ATmega328P cpu = new ATmega328P();

	public void step(int n) {
		
		for(int i = 0; i < n; i++) {
			if (cpu.hasInstructions()) cpu.execute();
			x++;
		//}
			System.out.println("\n[Arduino->run()] Iteration #" + (x-1));
			System.out.println("[Arduino] PC: 0x" + Integer.toHexString(cpu.program_counter) + " InstID: " + cpu.current_instruction_id
					+ " Instr: " + cpu.getInstructionById(cpu.current_instruction_id).name);
			System.out.print("[Arduino] [offset 0x24]: ");
			for (int j = 0; j < 8; j++) {
				//System.out.print(i + ": " + BinaryFunctions.byteToBoolArray(this.sram.readByte(0x24))[i] + " ");
				System.out.print(j + ": " + (((cpu.sram.readByte(0x24) >> j) & 0x1) == 1) + " ");
			}
			System.out.print("\n[Arduino] [offset 0x25]: ");
			for (int j = 0; j < 8; j++) {
				System.out.print(j + ": " + BinaryFunctions.byteToBoolArray(cpu.sram.readByte(0x25))[j] + " ");
			}
			System.out.println(getCpuRegisters());
			System.out.println(getXYZPointers()); // + " " + getStackInfo());
			System.out.println(getCpuFlags());
			System.out.println("[Arduino] Timer counter: " + cpu.timer0.getCounter());
			System.out.println("[Arduino] TOIE0: " + cpu.getFlag(cpu.TIMSK0, cpu.TOIE0) + " TOV0:" + cpu.getFlag(cpu.TIFR0, cpu.TOV0));
			
		}
	}	
		

	public void run() {
		
		while(cpu.hasInstructions()) {
			System.out.println("\n[Arduino->run()] Iteration #" + x);
	
			cpu.execute();
			
			System.out.print("[Arduino] [offset 0x24]: ");
			for (int j = 0; j < 8; j++) {
				//System.out.print(i + ": " + BinaryFunctions.byteToBoolArray(this.sram.readByte(0x24))[i] + " ");
				System.out.print(j + ": " + (((cpu.sram.readByte(0x24) >> j) & 0x1) == 1) + " ");
			}
			System.out.print("\n[Arduino] [offset 0x25]: ");
			for (int j = 0; j < 8; j++) {
				System.out.print(j + ": " + BinaryFunctions.byteToBoolArray(cpu.sram.readByte(0x25))[j] + " ");
			}
		
			System.out.println(getCpuRegisters());
			System.out.println(getXYZPointers()); // + " " + getStackInfo());
			System.out.println(getCpuFlags());
			System.out.println("[Arduino] TOIE0: " + cpu.getFlag(cpu.TIMSK0, cpu.TOIE0) + " TOV0:" + cpu.getFlag(cpu.TIFR0, cpu.TOV0));
			
			try{Thread.sleep(0,1);}catch(Exception e) {};
			
			x++;

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
			s += "R" + i + ": " + (cpu.sram.readByte(i) & 0xff) + " ";
		}
		s += "\n";
		for (int i = 17; i < 32; i++) {
			s += "R" + i + ": " + (cpu.sram.readByte(i) & 0xff) + " ";
		}
		return s;
	}

	public String getCpuFlags() {
		//C Z N V S H T I  sreg 0x5f		
		String s = "C Z N V S H T I\n";		
		for (int i = 0; i < 8; i++)
			s += (BinaryFunctions.getBit(cpu.sram.readByte(0x5f), i)) ? "1 " : "0 ";
		return s;
	}

	public String getXYZPointers() {
		return "X: " + cpu.getXRegister() + " (0x" + Integer.toHexString(cpu.getXRegister()) + ") -> " + cpu.sram.readByte(cpu.getXRegister())
				+ " Y: " + cpu.getYRegister() + " (0x" + Integer.toHexString(cpu.getYRegister()) + ") -> " + cpu.sram.readByte(cpu.getYRegister())
				+ " Z: " + cpu.getZRegister() + " (0x" + Integer.toHexString(cpu.getZRegister()) + ") -> " + cpu.sram.readByte(cpu.getZRegister());
	}

	public String getOffsetValue(String offset) {
		return "[SRAM " + offset + "] 0x" + Integer.toHexString(cpu.sram.readByte(Integer.parseInt(offset.substring(2), 16) & 0xff));
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
