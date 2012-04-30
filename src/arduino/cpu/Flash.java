package arduino.cpu;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import arduino.BinaryFunctions;

public class Flash {
	
	byte[] i_mem = new byte[(64*1024)]; // 64 kb instruction memory	
	//byte[] p_mem = new byte[(32*1024)-1]; // 32 kb program memory
	
	public int i_count = 0; // # of instructions loaded
	
	public Flash(String hexFile) {
		try {
			this.loadHexFile(hexFile);
			//loadHexFile("/Users/h4x/Desktop/CoderLvL_Asian/disassemblies/code2/Blink.cpp.hex");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("[Flash()] " + i_count + " instructions loaded consuming 0x" + Integer.toHexString(i_count*2) + " (" + i_count*2 + ") bytes.\n");
		//dumpInstructionMemory();
		
		//int offset = 0x32f;
		//System.out.println("[Flash()] offset 0x" + Integer.toHexString(offset & 0xffff) + ": 0x" + Integer.toHexString((this.i_mem[offset] & 0xff)));
		
		
	}
	
	
	public int readWordFromInstructionMemory(int offset) {
		
		System.out.println("[getWordFromInstrucionMemory()] Returning 16 bits: 0x"
				+ Integer.toHexString(((this.i_mem[offset] & 0xff) << 8) | (this.i_mem[offset+1] & 0xff)));
		
		return ((this.i_mem[offset] & 0xff) << 8) | (this.i_mem[offset+1] & 0xff);
		
	}
	
	public int readByteFromInstructionMemory(int offset) {
		
		return ((offset & 0x1) == 0) ? (this.i_mem[offset+1] & 0xff) : (this.i_mem[offset-1] & 0xff); 
		
	}
		
	public void loadHexFile(String hexFile) throws Exception {

		String strLine;
		int lineNumber = 0;
		FileInputStream fstream = new FileInputStream(hexFile);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		while ((strLine = br.readLine()) != null) {
			
			// Intel Hex Format
			// < : 1b> <data length 1b> <offset 2b> < type 1b > <data nb> <checksum 1b>
			// 

			switch(Integer.parseInt(strLine.substring(7,9))) { // RECTYPE

			case 0:
				int datalen = Integer.parseInt(strLine.substring(1,3), 16); //RECLEN

				byte data[] = BinaryFunctions.hexStringToByteArray(strLine.substring(9, 9+datalen*2));

				// Switch every two bytes - fix Endianness
				for (int i = 0; i < data.length-1; i+=2 ) {
					byte b = data[i];
					data[i] = data[i+1];
					data[i+1] = b;
				}

				
				for (int i = 0; i< data.length; i++) {
					
				
					this.i_mem[lineNumber + i] = (byte) (data[i] & 0xff);
					if (i %2 == 0) this.i_count++;
				
				
				}
				
				
				
				/*
				for (int i = 0; i < data.length/2; i+=2) {
					
					this.i_mem[i] = data[i];
					
					//this.i_mem[this.i_count] = (byte) (data[i] << 8 | data[i+1]);
					//this.i_count++;
				}
				*/
				
				
				
				break;
				

			case 1: // EOF
				break;
			
			}
		
			lineNumber += 0x10;
		}

	}	

	
	public void dumpInstructionMemory() {
		for (int i = 0; i < this.i_count*2; i++) {
			System.out.printf("0x%s: %s\n", Integer.toHexString(i), Integer.toHexString(this.i_mem[i] & 0xff));
		}
	}
	
}
