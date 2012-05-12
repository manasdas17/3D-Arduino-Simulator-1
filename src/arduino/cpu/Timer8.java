package arduino.cpu;

public class Timer8 implements ATmega328P_Definitions {
	
	MegaAVR cpu;

	// internal variables
	final int MAX = 0xFF;
	final int BOTTOM = 0x00;
	int TOP = 0xFF;

	// high-level execution variables
	int counter;
	int clock = 0;
	int prescaler = 1;
	int clockSelect = 0;
	int wgm_mode = 0;

	public Timer8(MegaAVR cpu) {
		this.cpu = cpu;
	}
	
	//debug
	
	public int getCounter() {
		return this.counter;
	}
	
	public void update() {

			switch (this.clockSelect = (cpu.sram.readByte(TCCR0B) & 0x7)) {	// Clock Select Bits
			case 0: return; 
			case 1: this.prescaler = 1;		break;
			case 2: this.prescaler = 8;		break;
			case 3: this.prescaler = 64;	break;
			case 4: this.prescaler = 256;	break;
			case 5: this.prescaler = 1024;	break;
			case 6: /* External clock source on T0 pin, falling edge */ break;
			case 7: /* External clock source on T0 pin, rising edge */ break;

			}
			

			if ((this.clock % this.prescaler == 0) && (this.clockSelect < 6)) { // external clocks to be dealt with later...

				switch (this.wgm_mode = (cpu.sram.readByte(TCCR0A) & 0x03) | (cpu.sram.readByte(TCCR0B) & 0x04)) {

				case 3:
					this.TOP = 0xFF;
					this.mode_FastPWM();
					break;
				case 7: 
					this.TOP = cpu.sram.readByte(OCR0A);
					this.mode_FastPWM();
					break;

				}
				
			}
			
			//System.out.println("\n\n[Timer8->execute()] wgm_mode: " + this.wgm_mode);
			
			//System.out.println("[timer0->mode_FastPWM()] counter: " + (counter & 0xff));

			this.clock++;
	}

	void mode_FastPWM() {	// Fast PWM

	
		counter = this.cpu.sram.readByte(TCNT0);	
		
		

		counter = (counter == TOP) ? 0 : counter+1;
		this.cpu.sram.writeByte(TCNT0, counter);

		if (counter == 0) {
			cpu.setFlag(TIFR0, TOV0);
			//System.out.println("#####[timer->mode_FastPWM]: Timer0 OVERFLOW - TOV0 Flag Set.");
		}

		// Compare Output Mode A
		switch (this.cpu.sram.readByte(TCCR0A) >> 6 & 0x3) {

		case 0:	break; /* Normal port Operation, OC0A disconnected */ 
		case 1: break;
		case 2: break;
		case 3: break;

		}

		// Compare Output Mode B
		switch (this.cpu.sram.readByte(TCCR0A) >> 4 & 0x3) {

		case 0:	break; /* Normal port Operation, OC0B disconnected */ 
		case 1: break;
		case 2: break;
		case 3: break;

		}


	}
}