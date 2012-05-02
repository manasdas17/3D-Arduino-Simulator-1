package arduino.cpu;

public interface ATmega328P_Definitions {

	// Data Space Offsets
	final int	PINB =		0x23;
	final int	DDRB =		0x24;
	final int	PORTB =		0x25;
	final int	PINC =		0x26;
	final int	DDRC =		0x27;
	final int	PORTC =		0x28;
	final int	PIND =		0x29;
	final int	DDRD =		0x2A;
	final int	PORTD =		0x2B;

	//TIFR0 - Timer/Counter 0 Interrupt Flag Register
	final int	TIFR0 =		0x35;	// Timer/Counter0 Interrupt Flag Register
	final byte	TOV0 =		0x0;	// Timer/Counter0 Overflow Flag
	final byte	OCF0A =		0x1;	// Timer/Counter0 Output Compare A Match Flag
	final byte	OCF0B =		0x2;	// Timer/Counter0 Output Compare B Match Flag

	final int	TIFR1 = 	0x36;
	final int	TIFR2 =		0x37;	
	final int	PCIFR = 	0x3B;
	final int	EIFR =		0x3C;
	final int	EIMSK =		0x3D;
	final int	GPIOR0 = 	0x3E;
	final int	EECR =		0x3F;
	final int	EEDR =		0x40;
	final int	EEARL = 	0x41;
	final int	EEARH =		0x42;
	final int	GTCCR =		0x43;

	//TCCR0A - Timer/Counter Control Register A
	final int	TCCR0A =	0x44;
	final byte	WGM00 =		0x00;	// Waveform Generation Mode
	final byte	WGM01 =		0x01;
	final byte	COM0B0 =	0x04;	// Compare Match Output B Mode
	final byte	COM0B1 =	0x05;
	final byte	COM0A0 =	0x06;	// Compare Match Output A Mode
	final byte	COM0A1 =	0x07;

	//TCCR0B - Timer/Counter Control Register B
	final int	TCCR0B =	0x45;
	final byte	CS00 =		0x00;	// Clock Select
	final byte	CS01 =		0x01;
	final byte	CS02 =		0x02;
	final byte	WGM02 =		0x03;	// Waveform Generation Mode
	final byte	FOC0B =		0x06;	// Force Output Compare B
	final byte	FOC0A =		0x07;	// Force Output Compare A

	//TCNT0 - Timer/Counter Register
	final int	TCNT0 =		0x46;

	//OCR0A - Output Compare Register A
	final int	OCR0A =		0x47;

	//OCR0B - Output Compare Register B
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
	//final int	SPL =		0x5D;
	//final int	SPH =		0x5E;
	//final int	SREG =		0x5F;
	final int	WDTCSR =	0x60;
	final int	CLKPR =		0x61;
	final int	PRR =		0x64;
	final int	OSCCAL =	0x66;
	final int	PCICR =		0x68;
	final int	EICRA =		0x69;
	final int	PCMSK0 =	0x6B;
	final int	PCMSK1 =	0x6C;
	final int	PCMSK2 =	0x6D;

	//TIMSK0 - Timer/Counter Interrupt Mask Register
	final int	TIMSK0 =	0x6E;
	final byte	TOIE0 =		0x00;	// Timer/Counter0 Overflow Interrupt Enable
	final byte	OCIE0A =	0x01;	// Timer/Counter0 Output Compare Match A Interrupt Enable
	final byte	OCIE0B =	0x02;	// Timer/Counter0 Output Compare Match B Interrupt Enable 


	final int	TIMSK1 =	0x6F;
	final int	TIMSK2 =	0x70;
	final int	ADCL =		0x78;
	final int	ADCH =		0x79;
	final int	ADCSRA =	0x7A;
	final int	ADCSRB =	0x7B;
	final int	ADCMUX =	0x7C;
	final int	DIDR0 =		0x7E;
	final int	DIDR1 =		0x7F;
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
	final int	UCSR0A =	0xC0;
	final int	UCSR0B =	0xC1;
	final int	UCSR0C =	0xC2;
	final int	UBRR0L =	0xC4;
	final int	UBRR0H =	0xC5;
	final int	UDR0 =		0xC6;


}
