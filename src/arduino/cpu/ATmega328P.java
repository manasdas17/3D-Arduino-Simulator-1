package arduino.cpu;

import arduino.BinaryFunctions;

public class ATmega328P extends MegaAVR {

	public ATmega328P() {
		
		sram = new SRAM();
		
		flash = new Flash("/Users/h4x/Desktop/CoderLvL_Asian/disassemblies/code2/Blink.cpp.hex");
		
		
		this.SREG = 0x5f;
		this.SPL = 0x5d;
		this.SPH = 0x5e;
		
		
		
		// debug - initialize TIFR0->TOV0
		//this.sram[0x35] = 0x1;
		//this.sram[0x46] = (byte) 0xff;
	}
	
	public boolean hasInstructions() {
		return this.program_counter <= this.flash.i_count*2 ? true : false;
	}

	public int noOfLoadedInstructions() {
		return flash.i_count;
	}

	
	public void execute() {

		//******TRACKING PORT B ************//


		System.out.print("##[cpu.execute()] [offset 0x24]: ");
		for (int i = 0; i < 8; i++) {
			System.out.print(i + ": " + BinaryFunctions.byteToBoolArray(this.sram.readByte(0x24))[i] + " ");
		}
		System.out.print("\n##[cpu.execute()] [offset 0x25]: ");
		for (int i = 0; i < 8; i++) {
			System.out.print(i + ": " + BinaryFunctions.byteToBoolArray(this.sram.readByte(0x25))[i] + " ");
		}
		System.out.println();


		//System.out.println("[cpu.execute()] instruction: " + Integer.toHexString(this.flash.readWordFromInstructionMemory(this.program_counter)));

		//this.instruction_register = 0xffff & (this.flash.i_mem[this.program_counter] << 8 | this.flash.i_mem[this.program_counter+1]);


		this.instruction_register = this.flash.readWordFromInstructionMemory(this.program_counter);

		//System.out.println("[cpu execute()] Instruction Register Dump: " + Integer.toHexString(instruction_register));


		this.current_instruction_id = getInstructionId(this.instruction_register);

		System.out.print("[cpu.execute()] PC: 0x" + Integer.toHexString(this.program_counter) + " InstID: " + this.current_instruction_id);

		//if (this.current_instruction_id != -1) System.out.println(" Instruction: " + Instructions[this.current_instruction_id].name);

		if (this.current_instruction_id != -1) System.out.println(" Instruction: " + this.getInstructionById(this.current_instruction_id).name);


		/*
		System.out.print("[cpu.execute()] PC: " + this.program_counter + " InstID: " + this.current_instruction_id);

		if (this.current_instruction_id != -1)
			System.out.print(" InstName: " + Instructions[this.current_instruction_id].name + "\n");
		 */

		callInstruction();
		//System.out.println("[cpu.execute()] executed runInstruction()\n");
		//System.out.println();

	}
	
	///////////////////////////// Definitions \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

	//Data Space
	//0x20
	final int	PINB =		0x23;
	final int	DDRB =		0x24;
	final int	PORTB =		0x25;
	final int	PINC =		0x26;
	final int	DDRC =		0x27;
	final int	PORTC =		0x28;
	final int	PIND =		0x29;
	final int	DDRD =		0x2A;
	final int	PORTD =		0x2B;
	final int	TIFR0 =		0x35;
	final int	TIFR1 = 	0x36;
	final int	TIFR2 =		0x37;	
	final int	PCIFR = 	0x3B;
	final int	EIFR =		0x3C;
	final int	EIMSK =		0x3D;
	final int	GPIOR0 = 	0x3E;
	final int	EECR =		0x3F;
	//0x40
	final int	EEDR =		0x40;
	final int	EEARL = 	0x41;
	final int	EEARH =		0x42;
	final int	GTCCR =		0x43;
	final int	TCCR0A =	0x44;
	final int	TCCR0B =	0x45;
	final int	TCNT0 =		0x46;
	final int	OCR0A =		0x47;
	final int	OCR0B =		0x48;
	final int	GPIOR1 =	0x4A;
	final int	GPIOR2 =	0x4B;
	final int	SPCR =		0x4C;
	final int	SPSR =		0x4D;
	final int	SPDR =		0x4E;
	final int	ACSR =		0x50;
	final int	SMCR =		0x53;
	final int	MCUSR =		0x54;
	final int	MCUCR =		0x55;
	final int	SPMCSR =	0x57;
	
	/*
	int	SPL =		0x5D;
	int	SPH =		0x5E;
	int	SREG =		0x5F;
	*/
	
	//0x60
	final int	WDTCSR =	0x60;
	final int	CLKPR =		0x61;
	final int	PRR =		0x64;
	final int	OSCCAL =	0x66;
	final int	PCICR =		0x68;
	final int	EICRA =		0x69;
	final int	PCMSK0 =	0x6B;
	final int	PCMSK1 =	0x6C;
	final int	PCMSK2 =	0x6D;
	final int	TIMSK0 =	0x6E;
	final int	TIMSK1 =	0x6F;
	final int	TIMSK2 =	0x70;
	final int	ADCL =		0x78;
	final int	ADCH =		0x79;
	final int	ADCSRA =	0x7A;
	final int	ADCSRB =	0x7B;
	final int	ADCMUX =	0x7C;
	final int	DIDR0 =		0x7E;
	final int	DIDR1 =		0x7F;
	//0x80
	final int	TCCR1A =	0x80;
	final int	TCCR1B =	0x81;
	final int	TCCR1C =	0x82;
	final int	TCNT1L =	0x84;
	final int	TCNT1H =	0x85;
	final int	ICR1L =		0x86;
	final int 	ICR1H =		0x87;
	final int	OCR1AL =	0x88;
	final int	OCR1AH =	0x89;
	final int	OCR1BL =	0x8A;
	//0xA0
	final int	OCR1BH =	0x8B;
	final int	TCCR2A =	0xB0;
	final int	TCCR2B =	0xB1;
	final int	TCNT2 =		0xB2;
	final int	OCR2A =		0xB3;
	final int	OCR2B =		0xB4;
	final int	ASSR =		0xB6;
	final int	TWBR =		0xB8;
	final int	TWSR =		0xB9;
	final int	TWAR =		0xBA;
	final int	TWDR =		0xBB;
	final int	TWCR =		0xBC;
	final int	TWAMR =		0xBD;
	//0xC0
	final int	UCSR0A =	0xC0;
	final int	UCSR0B =	0xC1;
	final int	UCSR0C =	0xC2;
	final int	UBRR0L =	0xC4;
	final int	UBRR0H =	0xC5;
	final int	UDR0 =		0xC6;

}
