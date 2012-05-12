package arduino.cpu;

public class ATmega328P extends MegaAVR implements ATmega328P_Definitions {

	public Timer8 timer0;	// change this to private	
	public Thread t1;
	
	public ATmega328P() {
		
		sram = new SRAM();
		timer0 = new Timer8(this);
		
		//flash = new Flash("/Users/h4x/Desktop/CoderLvL_Asian/disassemblies/Blink/Blink.cpp.hex");
		flash = new Flash("/tmp/build23504.tmp/Blink.cpp.hex ");
		
		this.SPL =	0x5D;
		this.SPH =	0x5E;
		this.SREG =	0x5F;
		
		pins = new boolean[28+1];
	
	}
	
	public void execute() {	
		
		System.out.println();

		this.instruction_register = this.flash.readWordFromInstructionMemory(this.program_counter);

		this.current_instruction_id = getInstructionId(this.instruction_register);

		callInstruction();
		
		updateModules();
		
		handleInterrupts();
		
		updatePins();
		
		//System.out.println("[cpu] Timer counter: " + this.timer0.getCounter());
	}
	
	// Modules
	
	private void updatePins() {
		
		this.pins[PB0] = this.getFlag(PORTB, PORTB0);
		this.pins[PB1] = this.getFlag(PORTB, PORTB1);
		this.pins[PB2] = this.getFlag(PORTB, PORTB2);
		this.pins[PB3] = this.getFlag(PORTB, PORTB3);
		this.pins[PB4] = this.getFlag(PORTB, PORTB4);
		this.pins[PB5] = this.getFlag(PORTB, PORTB5);
		this.pins[PB6] = this.getFlag(PORTB, PORTB6);
		this.pins[PB7] = this.getFlag(PORTB, PORTB7);
		
		this.pins[PC0] = this.getFlag(PORTC, PORTC0);
		this.pins[PC1] = this.getFlag(PORTC, PORTC1);
		this.pins[PC2] = this.getFlag(PORTC, PORTC2);
		this.pins[PC3] = this.getFlag(PORTC, PORTC3);
		this.pins[PC4] = this.getFlag(PORTC, PORTC4);
		this.pins[PC5] = this.getFlag(PORTC, PORTC5);
		
	}
	
	private void updateModules() {
		
		final int clkIO_prescaler = 128;
		
		for (int i = 0; i < this.clock*clkIO_prescaler; i++) {
			timer0.update();
		}
		
		this.clock = 0;
		
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
			this.clearFlag(TIFR0, TOV0);
			this.callInterrupt(0x40);
			this.updateClock(2);
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
