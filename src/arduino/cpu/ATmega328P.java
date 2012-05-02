package arduino.cpu;

import arduino.BinaryFunctions;

public class ATmega328P extends MegaAVR implements ATmega328P_Definitions {

	public Timer8 timer0;	// change this to private
	
	public ATmega328P() {
		
		sram = new SRAM();
		
		flash = new Flash("/Users/h4x/Desktop/CoderLvL_Asian/disassemblies/Blink/Blink.cpp.hex");
		
		//timer0 = new Timer_8Bit(this);
		
		this.SPL =	0x5D;
		this.SPH =	0x5E;
		this.SREG =	0x5F;
		
		timer0 = new Timer8(this);
		new Thread(timer0).start();
		
		//pins = new boolean[28];
		
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

		this.instruction_register = this.flash.readWordFromInstructionMemory(this.program_counter);

		this.current_instruction_id = getInstructionId(this.instruction_register);

		System.out.print("[cpu.execute()] PC: 0x" + Integer.toHexString(this.program_counter) + " InstID: " + this.current_instruction_id);

		if (this.current_instruction_id != -1) System.out.println(" Instruction: " + this.getInstructionById(this.current_instruction_id).name);

		callInstruction();

		handleInterrupts();
		
	}
	
	// Interrupts	
	private void handleInterrupts() {
		
		if (!this.getStatusFlag(I)) return;
		
		// 1 RESET
		// 2 INT 0			- External Interrupt Request 0
		// 3 INT 1			- External Interrupt Request 1
		// 4 PCINT0 		- Pin Change Interrupt Request 0
		// 5 PCINT1 		- Pin Change Interrupt Request 1
		// 6 PCINT2			- Pin Change Interrupt Request 2
		// 7 WDT			- Watchdog Timeout Interrupt
		// 8 TIMER2 COMPA	- Timer/Counter2 Compare Match A
		// 9 TIMER2 COMPB	- Timer/Counter2 Compare Match B
		// 10 TIMER2 OVF	- Timer/Counter2 Overflow
		// 11 TIMER1 CAPT	- Timer/Counter1 Capture Event
		// 12 TIMER1 COMPA	- Timer/Counter1 Compare Match A
		// 13 TIMER1 COMPB	- Timer/Counter1 Compare Match B
		// 14 TIMER1 OVF	- Timer/Counter1 Overflow
		// 15 TIMER0 COMPA	- Timer/Counter0 Compare Match A
		// 16 TIMER0 COMPB	- Timer/Counter0 Compare Match B

		// 17 TIMER0 OVF	- Timer/Counter0 Overflow		
		if (this.getFlag(TIMSK0, TOIE0) & this.getFlag(TIFR0, TOV0)) {
			
			this.setStatusFlag(I, false);
			this.callInterrupt(0x40);
			this.clearFlag(TIMSK0, TOV0);
		}
		
		// 18 SPI STC		- SPI Serial Transfer Complete
		// 19 USART RX		- USART Rx Complete
		// 20 USART UDRE	- USART Data Register Empty
		// 21 USART TX		- USART Tx Complete
		// 22 ADC			- ACD Conversion Complete
		// 23 EE READY		- EEPROM Ready
		// 24 ANALOG COMP	- Analog Comparator
		// 25 TWI			- 22-wire Serial Interface
		// 26 SPM READY		- Store Program Memory Ready
		
		
	}

	private void callInterrupt(int offset) {
		this.writeWordToStack(this.program_counter);
		this.setStackPointer(this.getStackPointer()-2);
		this.program_counter = offset;
	}

}
