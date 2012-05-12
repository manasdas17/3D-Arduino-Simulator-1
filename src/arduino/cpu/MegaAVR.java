package arduino.cpu;

import arduino.BinaryFunctions;

public abstract class MegaAVR {

	/*
	 * Stuff common to all megaAVR's.
	 * 
	 * Excluding: ATmega8515, ATmega103, ATmega603
	 * (MUL, MULS, MULSU, FMUL, FMULS, FMULSU, MOVW, LPM, SPM)
	 * 
	 */

	public SRAM sram;		// change this later to protected
	protected Flash flash;
	protected boolean[] pins;

	// offsets to be defined in child class
	protected byte SREG;
	protected byte SPH, SPL;

	//Status Register Flags
	protected final byte C = 0x0;
	protected final byte Z = 0x1;
	protected final byte N = 0x2;
	protected final byte V = 0x3;
	protected final byte S = 0x4;
	protected final byte H = 0x5;
	protected final byte T = 0x6;
	protected final byte I = 0x7;

	// execution variables
	public int program_counter;			// chage to protected after debug
	protected int instruction_register;	
	public int current_instruction_id;	//change to protected after debug
	protected int clock = 0;

	// high-level functions
	public boolean hasInstructions() {
		return this.program_counter <= this.flash.i_count*2 ? true : false;
	}

	public int noOfLoadedInstructions() {
		return flash.i_count;
	}

	public static int getInstructionId(int instr) {

		for (int i = 0; i < Instructions.length; i++) {
			if ((instr & Instructions[i].bitmask) == Instructions[i].code) 
				return Instructions[i].id;
		}

		return -1;
	}

	// very extremely inefficient code for getting the instruction name.
	public Instruction getInstructionById(int id) {
		for (int i = Instructions.length-1; i > -1; i--) {
			if ( Instructions[i].id == id) return Instructions[i];
		}
		return null;
	}

	public int getInstructionWordLength(int instr) {
		return Instructions[getInstructionId(instr)].words;
	}

	public int decodeInstructionParameter(char ch) {

		//System.out.print("[MegaAVR->decodeInstructionParameter()] variable: " + ch);

		String bitmask = "";
		String parameter = "";
		String instruction = Integer.toBinaryString(this.instruction_register);
		String formatString = this.getInstructionById(current_instruction_id).formatString;

		for (int i = instruction.length(); i < 16; i++) 
			instruction = "0" + instruction;

		for (int i = 0; i< 16; i++) 	
			bitmask += (formatString.replace(" ", "").charAt(i) == ch) ? "1" : "0";

		for (int i = 15; i > -1; i--) {
			if (bitmask.charAt(i) == '1') parameter = instruction.charAt(i) + parameter;	
		}

		//System.out.println(" value: 0x" + Integer.toHexString(Integer.parseInt(parameter, 2))
		//		+ " (" + Integer.parseInt(parameter,2) + ")");

		return Integer.parseInt(parameter, 2);

	}
	
	public boolean getPin(int n) {
		return this.pins[n];
	}


	/////////////////////////////////////////// Internal Operations \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

	protected void updateClock(int cycles) {
		this.clock+= cycles;
	}

	protected void incrementProgramCounter(int k) {
		this.program_counter += k*0x2;
	}

	// Status Register
	public boolean getStatusFlag(byte flag) {
		return BinaryFunctions.getBit(this.sram.readByte(this.SREG), flag) ? true : false;
	}

	protected void setStatusFlag(byte flag, boolean value) {
		this.sram.writeByte(this.SREG, BinaryFunctions.setBit(this.sram.readByte(this.SREG), flag, value));
	}

	// Cpu Flags
	public boolean getFlag(int offset, byte flag) {
		return BinaryFunctions.getBit(this.sram.readByte(offset), flag);
	}

	protected void setFlag(int offset, byte flag) {
		this.sram.writeByte(offset, BinaryFunctions.setBit(this.sram.readByte(offset), flag, true));
	}

	protected void clearFlag(int offset, byte flag) {	
		this.sram.writeByte(offset, BinaryFunctions.setBit(this.sram.readByte(offset), flag, false));
	}

	public int getXRegister() {
		return ((this.sram.readByte(27) << 8 & 0xff) | (this.sram.readByte(26) & 0xff));
	}

	public int getYRegister() {
		return ((this.sram.readByte(29) << 8 & 0xff) | (this.sram.readByte(28) & 0xff));
	}

	public int getZRegister() {
		return ((this.sram.readByte(31) << 8 & 0xff) | (this.sram.readByte(30) & 0xff));
	}

	protected void setXRegister(int n) {
		this.sram.writeByte(26, n & 0xff);
		this.sram.writeByte(27, (n >> 8) & 0xff); 	
	}
	protected void setYRegister(int n) {
		this.sram.writeByte(28, n & 0xff);
		this.sram.writeByte(29, (n >> 8) & 0xff); 	
	}
	protected void setZRegister(int n) {
		this.sram.writeByte(30, n & 0xff);
		this.sram.writeByte(31, (n >> 8) & 0xff); 	
	}

	//Stack operations
	public int getStackPointer() {
		return ((this.sram.readByte(this.SPH) & 0x3) << 8) | (this.sram.readByte(this.SPL) & 0xff);
	}

	protected void setStackPointer(int n) {	
		this.sram.writeByte(this.SPL, n & 0xff); 
		this.sram.writeByte(this.SPH, (this.sram.readByte(this.SPH) & 0xfc) | ((n >> 8) & 0x3));
	}


	public int readByteFromStack() {
		return this.sram.readByte(this.getStackPointer());
	}


	public int readWordFromStack() {
		return ((this.sram.readByte(this.getStackPointer()-1) & 0xff) << 8)
				| (this.sram.readByte(this.getStackPointer()) & 0xff);
	}

	protected void writeByteToStack(int n) {
		this.sram.writeByte(this.getStackPointer(), n & 0xff);
	}

	protected void writeWordToStack(int n) {
		this.sram.writeByte(this.getStackPointer()-1, (n >> 8) & 0xff);
		this.sram.writeByte(this.getStackPointer(), n & 0xff);
	}


	////////////////////////////////////////////// Instructions \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	protected void adc_command() {

		//parameters			
		int d,R;
		int Rd = this.sram.readByte(d = this.decodeInstructionParameter('d'));	// 0 <= r <= 31
		int Rr = this.sram.readByte(this.decodeInstructionParameter('r'));		// 0 <= r <= 31

		//operation
		this.sram.writeByte(d, R = this.getStatusFlag(C) ? Rr + Rd + 1 : Rr + Rd);

		//flags
		this.setStatusFlag(C, ((Rd&0x80) & (Rr&0x80) | (Rr&0x80) & ~(R&0x80) | ~(R&0x80) & (Rd&0x80)) == 0x80);
		this.setStatusFlag(Z, R == 0);
		this.setStatusFlag(N, (R&0x80) == 0x80);
		this.setStatusFlag(V, ((Rd&0x80) & (Rr&0x80) & ~(R&0x80) | ~(Rd&0x80) & ~(Rr&0x80) & (R&0x80)) == 0x80);
		this.setStatusFlag(S, getStatusFlag(N) ^ getStatusFlag(Z) );
		this.setStatusFlag(H, ((Rd&0x8) & (Rr&0x8) | (Rr&0x8) & ~(R&0x8) | ~(R&0x8) & (Rd&0x8)) == 0x8);

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(1);	

	}

	protected void add_command() {

		//parameters			
		int d,R;
		int Rd = this.sram.readByte(d = this.decodeInstructionParameter('d'));	// 0 <= r <= 31
		int Rr = this.sram.readByte(this.decodeInstructionParameter('r'));		// 0 <= r <= 31

		//operation
		this.sram.writeByte(d, R = Rr + Rd);

		//flags
		this.setStatusFlag(C, ((Rd&0x80) & (Rr&0x80) | (Rr&0x80) & ~(R&0x80) | ~(R&0x80) & (Rd&0x80)) == 0x80);
		this.setStatusFlag(Z, R == 0);
		this.setStatusFlag(N, (R&0x80) == 0x80);
		this.setStatusFlag(V, ((Rd&0x80) & (Rr&0x80) & ~(R&0x80) | ~(Rd&0x80) & ~(Rr&0x80) & (R&0x80)) == 0x80);
		this.setStatusFlag(S, getStatusFlag(N) ^ getStatusFlag(Z) );
		this.setStatusFlag(H, ((Rd&0x8) & (Rr&0x8) | (Rr&0x8) & ~(R&0x8) | ~(R&0x8) & (Rd&0x8)) == 0x8);

		//program counter
		incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}

	protected void adiw_command() {

		///parameters
		int d,R;
		int Rdh = this.sram.readByte(1+(d = 24 + 2*this.decodeInstructionParameter('d')));	// d = {24,26,28,30}
		int K = this.decodeInstructionParameter('K');		// 0 <= K <= 63

		//operation
		R = ((this.sram.readByte(d+1) << 8) | (this.sram.readByte(d))) + K;
		this.sram.writeByte(d, R & 0xff); 
		this.sram.writeByte(d+1, (R >> 8) & 0xff);

		//flags
		this.setStatusFlag(C, (((R&0x8000) & ~((Rdh&0x80)<<8)) == 0x8000));
		this.setStatusFlag(Z, R == 0);
		this.setStatusFlag(N, (R&0x8000) == 0x8000); 
		this.setStatusFlag(V, ((((Rdh&0x80)<<8) & ~(R&0x8000)) == 0x8000));
		this.setStatusFlag(S, this.getStatusFlag(N) ^ this.getStatusFlag(V));

		//program counter
		incrementProgramCounter(1);

		//clock
		this.updateClock(2);

	}

	protected void and_command() {

		//parameters
		int r = decodeInstructionParameter('r');	// 0 <= r <= 31
		int d = decodeInstructionParameter('d');	// 0 <= d <= 31

		//operation
		this.sram.writeByte(d, this.sram.readByte(d) & this.sram.readByte(r));

		//flags
		this.setStatusFlag(Z, (this.sram.readByte(d) == 0));
		this.setStatusFlag(N, (BinaryFunctions.getBit(this.sram.readByte(d), 7)));
		this.setStatusFlag(V, false);
		this.setStatusFlag(S, this.getStatusFlag(N) ^ this.getStatusFlag(Z) );

		//program counter
		incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}

	protected void andi_command() { 

		//parameters	
		int K = decodeInstructionParameter('K');		// 0 <= K <= 255
		int d = 16 + decodeInstructionParameter('d');	// 0 <= d <= 31

		//operation
		this.sram.writeByte(d, (this.sram.readByte(d) & K));

		//flags
		this.setStatusFlag(Z, (this.sram.readByte(d) == 0));
		this.setStatusFlag(N, (BinaryFunctions.getBit(this.sram.readByte(d), 7)));
		this.setStatusFlag(V, false);
		this.setStatusFlag(S, this.getStatusFlag(N) ^ this.getStatusFlag(V) );

		//program counter
		incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}

	protected void asr_command() { 


	}

	protected void bclr_command() { //Complete

		//parameters
		byte s = (byte) this.decodeInstructionParameter('s');	// 0 <= s <= 7

		//operation
		this.setStatusFlag(s, false);

		//program counter
		incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}

	protected void bld_command() { 

	}

	protected void brbc_command() { //complete

		//parameters
		int k = this.decodeInstructionParameter('k'); // -64 <= k <= 63
		if (k >= 0x40) k = -((~k+1) & 0x3f); 
		byte s = (byte) this.decodeInstructionParameter('s');  // 0 <= s <= 7

		//operation
		this.incrementProgramCounter(this.getStatusFlag(s) ? 1 : k+1);

		//clock
		this.updateClock(this.getStatusFlag(s) ? 1 : 2);

	}

	protected void brbs_command() { //complete

		//parameters
		int k = this.decodeInstructionParameter('k'); // -64 <= k <= 63
		if (k >= 0x40) k = -((~k+1) & 0x3f);
		byte s = (byte) this.decodeInstructionParameter('s');  // 0 <= s <= 7

		//operation
		this.incrementProgramCounter(this.getStatusFlag(s) ? k+1 : 1);

		//clocks
		this.updateClock(this.getStatusFlag(s) ? 2 : 1);

	}

	protected void brcc_command() {

		//parameters
		int k = this.decodeInstructionParameter('k'); // -64 <= k <= 63
		if (k >= 0x40) k = -((~k+1) & 0x3f);

		//operation
		this.incrementProgramCounter(this.getStatusFlag(C) ? 1 : k+1);

		//clocks
		this.updateClock(this.getStatusFlag(C) ? 1 : 2);

	}

	protected void brcs_command() {

		//parameters
		int k = this.decodeInstructionParameter('k'); // -64 <= k <= 63
		if (k >= 0x40) k = -((~k+1) & 0x3f);

		//operation
		this.incrementProgramCounter(this.getStatusFlag(C) ? k+1 : 1);

		//clock
		this.updateClock(this.getStatusFlag(C) ? 2 : 1);

	}
	protected void break_command() {

	}
	protected void breq_command() {

		//parameters
		int k = this.decodeInstructionParameter('k'); // -64 <= k <= 63
		if (k >= 0x40) k = -((~k+1) & 0x3f);

		//operation
		this.incrementProgramCounter(this.getStatusFlag(Z) ? k+1 : 1);

		//clock
		this.updateClock(this.getStatusFlag(Z) ? 2 : 1);

	}
	protected void brge_command() {

	}
	protected void brhc_command() {

	}
	protected void brhs_command() {

	}
	protected void brid_command() {

	}
	protected void brie_command() {

	}
	protected void brlo_command() {

	}
	protected void brlt_command() {

	}
	protected void brmi_command() {

	}
	protected void brne_command() {

		//parameters
		int k = this.decodeInstructionParameter('k');		// -64 <= k <= 63
		//System.out.println("[brne_command()] raw k: " + k);
		if (k >= 0x40) k = -((~k + 1) & 0x3F);
		//System.out.println("[brne_command()] modified k: " + k);

		//program counter
		this.incrementProgramCounter(this.getStatusFlag(Z) ? 1 : k+1);

		//clock
		this.updateClock(this.getStatusFlag(Z) ? 1 : 2);

	}
	protected void brpl_command() {

	}
	protected void brsh_command() {

	}
	protected void brtc_command() {

	}
	protected void brts_command() {

	}
	protected void brvc_command() {

	}
	protected void brvs_command() {

	}
	protected void bset_command() {

		//parameters
		byte s = (byte) this.decodeInstructionParameter('s');	// 0 <= s <= 7

		//operation
		this.setStatusFlag(s, true);

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}
	protected void bst_command() {

	}
	protected void call_command() {

		//parameters
		int k = (this.decodeInstructionParameter('k') << 8		// 0 <= k < 64K 
				| this.flash.readWordFromInstructionMemory(this.program_counter+2)) << 1;

		//operation
		this.writeWordToStack(this.program_counter + 2*0x2);
		this.setStackPointer(this.getStackPointer()-2);

		//program counter
		this.program_counter = k;

		//clock
		this.updateClock(4);

	}

	protected void cbi_command() {

	}
	protected void cbr_command() {

	}
	protected void clc_command() {

	}
	protected void clh_command() {

	}
	protected void cli_command() {

		//operation
		this.setStatusFlag(I, false);

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}
	protected void cln_command() {

	}
	protected void clr_command() {

	}
	protected void cls_command() {

	}
	protected void clt_command() {

	}
	protected void clv_command() {

	}
	protected void clz_command() {

	}
	protected void com_command() {

		//parameters
		int d = this.decodeInstructionParameter('d');

		//operation
		this.sram.writeByte(d, (0xff - this.sram.readByte(d)));

		//flags
		this.setStatusFlag(C, true);
		this.setStatusFlag(Z, (this.sram.readByte(d) == 0));
		this.setStatusFlag(N, ((this.sram.readByte(d) >> 7) & 0x1) == 1); 
		this.setStatusFlag(V, false);
		this.setStatusFlag(S, this.getStatusFlag(N) ^ this.getStatusFlag(V));

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}
	protected void cp_command() {

		//parameters
		int Rd = this.sram.readByte(this.decodeInstructionParameter('d'));
		int Rr = this.sram.readByte(this.decodeInstructionParameter('r'));

		//operation
		int R = (Rd - Rr) & 0xff;

		//flags
		this.setStatusFlag(C, (~(Rd&0x80) & (Rr&0x80) | (Rr&0x80) & (R&0x80) | (R&0x80) & ~(Rd&0x80)) == 0x80);		
		this.setStatusFlag(Z, R == 0);
		this.setStatusFlag(N, (R&0x80) == 0x80);
		this.setStatusFlag(V, ((Rd&0x80) & ~(Rr&0x80) & ~(R&0x80) | ~(Rd&0x80) & (Rr&0x80) & (R&0x80)) == 0x80);
		this.setStatusFlag(S, this.getStatusFlag(N) ^ this.getStatusFlag(V));
		this.setStatusFlag(H, (~(Rd&0x8) & (Rr&0x8) | (Rr&0x8) & (R&0x8) | (R&0x8) & ~(Rd&0x8)) == 0x8);

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}

	protected void cpc_command() {

		//parameters
		int Rd = this.sram.readByte(this.decodeInstructionParameter('d'));
		int Rr = this.sram.readByte(this.decodeInstructionParameter('r'));

		//operation
		int R = (this.getStatusFlag(C) ? Rd - Rr - 1 : Rd - Rr) & 0xff;

		//flags
		this.setStatusFlag(C, (~(Rd&0x80) & (Rr&0x80) | (Rr&0x80) & (R&0x80) | (R&0x80) & ~(Rd&0x80)) == 0x80);		
		if (R != 0) this.setStatusFlag(Z, false);
		this.setStatusFlag(N, (R&0x80) == 0x80);
		this.setStatusFlag(V, ((Rd&0x80) & ~(Rr&0x80) & ~(R&0x80) | ~(Rd&0x80) & (Rr&0x80) & (R&0x80)) == 0x80);
		this.setStatusFlag(S, this.getStatusFlag(N) ^ this.getStatusFlag(V));
		this.setStatusFlag(H, (~(Rd&0x8) & (Rr&0x8) | (Rr&0x8) & (R&0x8) | (R&0x8) & ~(Rd&0x8)) == 0x8);

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}


	protected void cpi_command() {
		
		//parameters
		int Rd = this.sram.readByte(this.decodeInstructionParameter('d')+0x10); 	// 16 <= d <= 31 
		int K = this.decodeInstructionParameter('K'); 								// 0 <= K <= 255

		int R = (Rd - K) & 0xff;

		//flags
		this.setStatusFlag(C, (~(Rd&0x80) & (K&0x80) | (K&0x80) & (R&0x80) | (R&0x80) & ~(Rd&0x80)) == 0x80);		
		this.setStatusFlag(Z, R == 0);
		this.setStatusFlag(N, (R&0x80) == 0x80);
		this.setStatusFlag(V, ((Rd&0x80) & ~(K&0x80) & ~(R&0x80) | ~(Rd&0x80) & (K&0x80) & (R&0x80)) == 0x80);
		this.setStatusFlag(S, this.getStatusFlag(N) ^ this.getStatusFlag(V));
		this.setStatusFlag(H, (~(Rd&0x8) & (K&0x8) | (K&0x8) & (R&0x8) | (R&0x8) & ~(Rd&0x8)) == 0x8);

		//Program Counter
		incrementProgramCounter(1);

		//clock
		this.updateClock(1);
		
	}
	protected void cpse_command() {

	}
	protected void dec_command() {

		//parameters
		int d = this.decodeInstructionParameter('d');

		//operation
		this.sram.writeByte(d, this.sram.readByte(d) - 1);

		//flags
		this.setStatusFlag(Z, this.sram.readByte(d) == 0);
		this.setStatusFlag(N, ((this.sram.readByte(d) >> 7) & 0x1 ) == 1);
		this.setStatusFlag(V, this.sram.readByte(d) == 0x7f);
		this.setStatusFlag(S, this.getStatusFlag(V) ^ this.getStatusFlag(N));

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}
	protected void des_command() {

	}
	protected void eicall_command() {

	}
	protected void eijmp_command() {

	}
	protected void elpm1_command() {

	}
	protected void elpm2_command() {

	}
	protected void elpm3_command() {

	}

	protected void eor_command() {

		//parameters
		int r = decodeInstructionParameter('r');	// 0 <= r <= 31
		int d = decodeInstructionParameter('d');	// 0 <= d <= 31

		//operation
		this.sram.writeByte(d, this.sram.readByte(d) ^ this.sram.readByte(r));

		//flags
		this.setStatusFlag(Z, this.sram.readByte(d) == 0);
		this.setStatusFlag(N, (BinaryFunctions.getBit(this.sram.readByte(d), 7)));
		this.setStatusFlag(V, false);
		this.setStatusFlag(S, this.getStatusFlag(N) ^ this.getStatusFlag(V) );

		//program counter
		incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}

	protected void fmul_command() {

	}
	protected void fmuls_command() {

	}
	protected void fmulsu_command() {

	}
	protected void icall_command() {

	}
	protected void ijmp_command() {

	}
	protected void in_command() {

		//parameters
		int d = this.decodeInstructionParameter('d');		// 0 <= d <= 31
		int A = this.decodeInstructionParameter('A');		// 0 <= A <= 63

		//operation
		this.sram.writeByte(d,this.sram.readByte(A+0x20));

		//System.out.println("[in_command()]: wrote + 0x" 
		//		+ Integer.toHexString(this.sram.readByte(d))
		//		+ " (" + this.sram.readByte(d) + ") into offset 0x"
		//		+ Integer.toHexString(d) + " (" + d +")");

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}
	protected void inc_command() {

	}

	protected void jmp_command() { //Complete

		// parameters
		//int k = ( (this.decodeInstructionParameter('k') << 16)
		//		| flash.i_mem[this.program_counter + 1] ) << 1;

		int k = ((this.decodeInstructionParameter('k') << 16)
				| this.flash.readWordFromInstructionMemory(this.program_counter+0x2)) << 1;

		//program counter
		//System.out.println("[jmp_command()] value of k: 0x" + Integer.toHexString(k));
		this.program_counter = k;

		//clock
		this.updateClock(1);

	}

	protected void lac_command() {

	}
	protected void las_command() {

	}
	protected void lat_command() {

	}

	/*
	LD instruction can load data from program memory since the flash is memory mapped. Loading data from the data memory
	takes 1 clock cycle, and loading from the program memory takes 2 clock cycles. But if an interrupt occur (before the last
	clock cycle) no additional clock cycles is necessary when loading from the program memory. Hence, the instruction takes
	only 1 clock cycle to execute.

	Think about that when implementing interrupts.
	 */

	protected void ldx1_command() {

		//parameters
		int d = this.decodeInstructionParameter('d');	// 0 <= d <= 31

		//operation
		this.sram.writeByte(d, this.sram.readByte(this.getXRegister()));

		//debug
		//System.out.println("[ldx1_command()] wrote 0x" + Integer.toHexString(this.sram.readByte(d) & 0xff)
		//		+ " (" + (this.sram.readByte(d) & 0xff) + ") into offset 0x" 
		//		+ Integer.toHexString((this.getXRegister()) & 0xffff) + " (" + d + ")");

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}
	protected void ldx2_command() {

	}
	protected void ldx3_command() {

	}
	protected void ldy1_command() {

	}
	protected void ldy2_command() {

	}
	protected void ldy3_command() {

	}
	protected void ldy4_command() {

	}
	protected void ldz1_command() {

		//parameters
		int d = this.decodeInstructionParameter('d');	// 0 <= d <= 31

		//operation
		this.sram.writeByte(d, this.sram.readByte(this.getZRegister()));

		//debug
		//System.out.println("[ldz1_command()] wrote 0x" + Integer.toHexString(this.sram.readByte(d) & 0xff)
		//		+ " (" + (this.sram.readByte(d) & 0xff) + ") into offset 0x" + Integer.toHexString(d)
		//		+ " (" + d + ")");

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}
	protected void ldz2_command() {

	}
	protected void ldz3_command() {

	}
	protected void ldz4_command() {

		//parameters
		int q = this.decodeInstructionParameter('q');		// 0 <= q <= 63
		int d = this.decodeInstructionParameter('d');		// 0 <= d <= 31

		//operation
		this.sram.writeByte(d, this.sram.readByte(this.getZRegister() + q));

		//debug
		/*
		System.out.println("[ldz4_command()] wrote 0x" + Integer.toHexString(this.sram.readByte(d) & 0xff)
				+ " (" + (this.sram.readByte(d) & 0xff) + ") into offset 0x" + Integer.toHexString(this.getZRegister() & 0xffff)
				+ " (" + (this.getZRegister() & 0xffff) + ")");
		 */

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(3);

	}
	protected void ldi_command() {

		//parameters
		int d = 16 + this.decodeInstructionParameter('d');
		int K = this.decodeInstructionParameter('K');

		//operation
		this.sram.writeByte(d, K);

		//debug
		//System.out.println("[ldi_command()] wrote 0x" + Integer.toHexString(this.sram.readByte(d) & 0xff)
		//		+ " (" + (this.sram.readByte(d) & 0xff) + ") into offset 0x" + Integer.toHexString(d)
		//		+ " (" + (d) + ")");

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}
	protected void lds_command() {

		//parameters
		int d = this.decodeInstructionParameter('d');
		int k = this.flash.readWordFromInstructionMemory(this.program_counter + 2);

		//operation
		this.sram.writeByte(d, this.sram.readByte(k));

		//program counter
		this.incrementProgramCounter(2);

		//clock
		this.updateClock(1);

	}
	protected void lds16_command() {

	}
	protected void lpm1_command() {

	}

	protected void lpm2_command() {

		//parameters
		int d = this.decodeInstructionParameter('d');		// 0 <= d <= 31

		//operation
		this.sram.writeByte(d, this.flash.readByteFromInstructionMemory(this.getZRegister()));

		//debug
		//System.out.println("[lpm2_command()] wrote 0x" + Integer.toHexString(this.sram.readByte(d) & 0xff)
		//		+ " (" + (this.sram.readByte(d) & 0xff) + ") into offset 0x" 
		//		+ Integer.toHexString(d & 0xff) + " (" + (d & 0xff) + ")");

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(3);

	}

	protected void lpm3_command() {

		//parameters
		int d = this.decodeInstructionParameter('d');		// 0 <= d <= 31

		//operation
		this.sram.writeByte(d, this.flash.readByteFromInstructionMemory(this.getZRegister()));
		this.setZRegister(this.getZRegister() + 1);

		//debug
		//System.out.println("[lpm3_command()] wrote 0x" + Integer.toHexString(this.sram.readByte(d) & 0xff)
		//		+ " (" + (this.sram.readByte(d) & 0xff) + ") into offset 0x" 
		//		+ Integer.toHexString(d & 0xff) + " (" + (d & 0xff) + ")");

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(3);

	}
	protected void lsl_command() {

	}
	protected void lsr_command() {

	}
	protected void mov_command() {

		//parameters
		int d = this.decodeInstructionParameter('d');	// 0 <= d <= 31
		int r = this.decodeInstructionParameter('r');	// 0 <= r <= 31

		//operation
		this.sram.writeByte(d, this.sram.readByte(r));

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}

	protected void movw_command() {

		//parameters
		int d = 2 * this.decodeInstructionParameter('d');	// d = {0,2,4,...,30}
		int r = 2 * this.decodeInstructionParameter('r');	// r = {0,2,4,...,30}

		//operation
		this.sram.writeByte(d, this.sram.readByte(r));
		this.sram.writeByte(d+1, this.sram.readByte(r+1));

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}

	protected void mul_command() {

	}
	protected void muls_command() {

	}
	protected void mulsu_command() {

	}
	protected void neg_command() {

	}
	protected void nop_command() {

	}

	protected void or_command() {

		//parameters
		int d = decodeInstructionParameter('d');	// 0 <= d <= 31
		int r = decodeInstructionParameter('r');	// 0 <= r <= 31

		//operation
		this.sram.writeByte(d, this.sram.readByte(d) | this.sram.readByte(r));

		//flags
		this.setStatusFlag(Z, (this.sram.readByte(d) == 0));
		this.setStatusFlag(N, (BinaryFunctions.getBit(this.sram.readByte(d), 7)));
		this.setStatusFlag(V, false);
		this.setStatusFlag(S, this.getStatusFlag(N) ^ this.getStatusFlag(Z) );

		//program counter
		incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}


	protected void ori_command() {

		//parameters
		int d = 16 + decodeInstructionParameter('d');	// 16 <= d <= 31
		int K = decodeInstructionParameter('K');		// 0 <= d <= 255

		//operation
		this.sram.writeByte(d, this.sram.readByte(d) | K);

		//flags
		this.setStatusFlag(Z, (this.sram.readByte(d) == 0));
		this.setStatusFlag(N, (BinaryFunctions.getBit(this.sram.readByte(d), 7)));
		this.setStatusFlag(V, false);
		this.setStatusFlag(S, this.getStatusFlag(N) ^ this.getStatusFlag(Z) );

		//program counter
		incrementProgramCounter(1);

		//clock
		this.updateClock(1);
	}

	protected void out_command() {

		//parameters
		int A = this.decodeInstructionParameter('A'); // 0 <= A <= 63
		int r = this.decodeInstructionParameter('r'); // 0 <= r <= 31

		//instruction
		this.sram.writeByte(A+0x20, this.sram.readByte(r));

		//debug
		//System.out.println("[out_command()] wrote 0x" + Integer.toHexString(this.sram.readByte(r) & 0xff)
		//		+ " (" + (this.sram.readByte(r) & 0xff) + ") into offset 0x" + Integer.toHexString(A+0x20)
		//		+ " (" + (A+0x20) + ")");

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}
	protected void pop_command() {

		//parameters
		int d = this.decodeInstructionParameter('d');

		//operation
		this.setStackPointer(this.getStackPointer()+1);
		this.sram.writeByte(d, this.readByteFromStack());

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(2);

	}

	protected void push_command() {

		//parameters
		int r = this.decodeInstructionParameter('r');

		//System.out.println("[push_command()]: About to push " + this.sram[r] + "[0x" + Integer.toHexString(r)
		//				+ "] to the stack pointed by [0x" + Integer.toHexString(this.stack_pointer) + "]");

		//operation
		//this.sram[this.stack_pointer] = this.sram[r];	// 0 <= r <= 31
		//this.stack_pointer -= 1;

		//System.out.println("[push_command()] About to push 0x" + Integer.toHexString((this.sram.readByte(r) & 0xff))
		//		+ " into offset 0x" + Integer.toHexString(this.getStackPointer() & 0xffff));

		this.writeByteToStack(this.sram.readByte(r) & 0xff);
		this.setStackPointer(this.getStackPointer()-1);

		//System.out.println("[push_command()] current stack_pointer: " + this.stack_pointer);

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(2);

	}
	protected void rcall_command() {


	}
	protected void ret_command() {

		//debug
		//System.out.println("[ret_command()] value of stack pointer: " + this.stack_pointer);

		//operation
		this.setStackPointer(this.getStackPointer()+2);
		this.program_counter = this.readWordFromStack();

		//clock
		this.updateClock(4);

	}

	protected void reti_command() {

		//operation
		this.setStackPointer(this.getStackPointer()+2);

		//flags
		this.setStatusFlag(I, true);

		//program counter
		this.program_counter = this.readWordFromStack();

		//clock
		this.updateClock(4);

	}

	protected void rjmp_command() {

		//parameters
		int k = this.decodeInstructionParameter('k');	// -2*1024 <= k <= 2*1024
		if (k >= 0x800) k = -((~k+1)) & 0x7ff;		// 2's complement

		//program counter
		this.incrementProgramCounter(k+1);

		//clock
		this.updateClock(1);

	}

	protected void rol_command() {

	}
	protected void ror_command() {

	}
	protected void sbc_command() {

		//parameters
		int R,d;
		int Rd = this.sram.readByte(d = this.decodeInstructionParameter('d'));
		int Rr = this.sram.readByte(this.decodeInstructionParameter('r'));
		
		//operation
		this.sram.writeByte(d, R = this.getStatusFlag(C) ? Rd - Rr - 1 : Rd - Rr);

		//flags
		this.setStatusFlag(C, (~(Rd&0x80) & (Rr&0x80) | (Rr&0x80) & (R&0x80) | (R&0x80) & ~(Rd&0x80)) == 0x80);		
		if (R != 0) this.setStatusFlag(Z, false);
		this.setStatusFlag(N, (R&0x80) == 0x80);
		this.setStatusFlag(V, ((Rd&0x80) & ~(Rr&0x80) & ~(R&0x80) | ~(Rd&0x80) & (Rr&0x80) & (R&0x80)) == 0x80);
		this.setStatusFlag(S, this.getStatusFlag(N) ^ this.getStatusFlag(V));
		this.setStatusFlag(H, (~(Rd&0x8) & (Rr&0x8) | (Rr&0x8) & (R&0x8) | (R&0x8) & ~(Rd&0x8)) == 0x8);

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}

	protected void sbci_command() {
		//parameters
		int d, R;
		int Rd = this.sram.readByte(d = this.decodeInstructionParameter('d')+0x10); // 16 <= d <= 32 
		int K = this.decodeInstructionParameter('K'); 								// 0 <= K <= 255

		//operation
		this.sram.writeByte(d, R = this.getStatusFlag(C) ? Rd - K - 1 : Rd - K);

		//debug
		//System.out.println("[sbci_command()] wrote 0x" + Integer.toHexString(this.sram.readByte(d) & 0xff)
		//		+ " (" + (this.sram.readByte(d) & 0xff) + ") into offset 0x" + Integer.toHexString(d)
		//		+ " (" + (d) + ")");

		//flags
		this.setStatusFlag(C, (~(Rd&0x80) & (K&0x80) | (K&0x80) & (R&0x80) | (R&0x80) & ~(Rd&0x80)) == 0x80);		
		if (R != 0) this.setStatusFlag(Z, false);
		this.setStatusFlag(N, (R&0x80) == 0x80);
		this.setStatusFlag(V, ((Rd&0x80) & ~(K&0x80) & ~(R&0x80) | ~(Rd&0x80) & (K&0x80) & (R&0x80)) == 0x80);
		this.setStatusFlag(S, this.getStatusFlag(N) ^ this.getStatusFlag(V));
		this.setStatusFlag(H, (~(Rd&0x8) & (K&0x8) | (K&0x8) & (R&0x8) | (R&0x8) & ~(Rd&0x8)) == 0x8);

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}
	protected void sbi_command() {

	}
	protected void sbic_command() {

	}
	protected void sbis_command() {

		//parameters
		int A = this.decodeInstructionParameter('A');	// 0 <= A <= 32
		int b = this.decodeInstructionParameter('b'); 	// 0 <= b <= 7

		//operation
		//Lower 32 I/O addresses -> A+0x20
		if (BinaryFunctions.getBit(this.sram.readByte(A+0x20), b)) {
			incrementProgramCounter(this.getInstructionWordLength(this.flash.readWordFromInstructionMemory(this.program_counter+2)));	
		}

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(BinaryFunctions.getBit(this.sram.readByte(A+0x20), b) ?
				(1 + this.getInstructionWordLength(this.flash.readWordFromInstructionMemory(this.program_counter))) : 1);

	}
	protected void sbiw_command() {

		//parameters
		int d,R;
		int Rdh = this.sram.readByte(1+(d = 24 + 2*this.decodeInstructionParameter('d')));	// d = {24,26,28,30}
		int K = this.decodeInstructionParameter('K');		// 0 <= K <= 63
		
		//operation
		R = ((this.sram.readByte(d+1) << 8) | this.sram.readByte(d)) - K;
		this.sram.writeByte(d+1, (R >> 8) & 0xff);
		this.sram.writeByte(d, R & 0xff);

		//debug
		//System.out.println("[sbiw_command()] wrote 0x" + Integer.toHexString(this.sram.readByte(d) & 0xff)
		//		+ " (" + (this.sram.readByte(d) & 0xff) + ") into offset 0x" + Integer.toHexString(d)
		//		+ " (" + (d) + ")");

		//System.out.println("[sbiw_command()] wrote 0x" + Integer.toHexString(this.sram.readByte(d+1) & 0xff)
		//		+ " (" + (this.sram.readByte(d+1) & 0xff) + ") into offset 0x" + Integer.toHexString(d+1)
		//		+ " (" + (d+1) + ")");

		//flags
		this.setStatusFlag(C, (((R&0x8000) & ~((Rdh&0x80)<<8)) == 0x8000));
		this.setStatusFlag(Z, R == 0);
		this.setStatusFlag(N, (R&0x8000) == 0x8000); 
		this.setStatusFlag(V, ((((Rdh&0x80)<<8) & ~(R&0x8000)) == 0x8000));
		this.setStatusFlag(S, this.getStatusFlag(N) ^ this.getStatusFlag(V));

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}
	protected void sbr_command() {

	}
	protected void sbrc_command() {

	}
	protected void sbrs_command() {

		//parameters
		int r = this.decodeInstructionParameter('r');	// 0 <= r <= 31
		int b = this.decodeInstructionParameter('b');	// 0 <= b <= 7 

		//operation
		if (BinaryFunctions.getBit(this.sram.readByte(r), b)) {
			incrementProgramCounter(this.getInstructionWordLength(this.flash.readWordFromInstructionMemory(this.program_counter+2)));	
		}

		//program counter
		incrementProgramCounter(1);

		//clock
		this.updateClock(BinaryFunctions.getBit(this.sram.readByte(r), b) ?
				(1 + this.getInstructionWordLength(this.flash.readWordFromInstructionMemory(this.program_counter))) : 1);

	}
	protected void sec_command() {

	}
	protected void seh_command() {

	}
	protected void sei_command() {

		//operation
		this.setStatusFlag(I, true);

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}
	protected void sen_command() {

	}
	protected void ser_command() {

	}
	protected void ses_command() {

	}
	protected void set_command() {

	}
	protected void sev_command() {

	}
	protected void sez_command() {

	}
	protected void sleep_command() {

	}
	protected void spm_command() {

	}
	protected void stx1_command() {

		//parameters
		int r = this.decodeInstructionParameter('r');

		//operation
		this.sram.writeByte(this.getXRegister(), this.sram.readByte(r));

		//debug
		//System.out.println("[stx2_command()] wrote 0x" + Integer.toHexString(this.sram.readByte(r) & 0xff)
		//		+ " (" + (this.sram.readByte(r) & 0xff) + ") into X Register (offset 0x" 
		//		+ Integer.toHexString(this.getXRegister() & 0xffff) + ")"
		//		+ " (" + (this.getXRegister() & 0xffff) + ")");

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(2);

	}
	protected void stx2_command() {

		//parameters
		int r = this.decodeInstructionParameter('r');

		//operation
		this.sram.writeByte(this.getXRegister(), this.sram.readByte(r));
		this.setXRegister(this.getXRegister() + 1);

		//debug
		//System.out.println("[stx2_command()] wrote 0x" + Integer.toHexString(this.sram.readByte(r) & 0xff)
		//		+ " (" + (this.sram.readByte(r) & 0xff) + ") into X Register (offset 0x" 
		//		+ Integer.toHexString((this.getXRegister()-1) & 0xffff) + ")"
		//		+ " (" + ((this.getXRegister()-1) & 0xffff) + ")");

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(2);

	}
	protected void stx3_command() {

	}
	protected void sty1_command() {


	}
	protected void sty2_command() {

	}
	protected void sty3_command() {

	}
	protected void sty4_command() {

	}
	protected void stz1_command() {

		//parameters
		int r = this.decodeInstructionParameter('r');

		//operation
		this.sram.writeByte(this.getZRegister(), this.sram.readByte(r));

		//debug
		//System.out.println("[stz1_command()] wrote 0x" + Integer.toHexString(this.sram.readByte(this.getZRegister()))
		//		+ " (" + this.sram.readByte(this.getZRegister()) + ") into Z Register (offset 0x" 
		//		+ Integer.toHexString(this.getZRegister() & 0xffff) + ")"
		//		+ " (" + (this.getZRegister() & 0xffff) + ")");

		System.out.println("[stz1_command()] offset 0x6e: " + this.sram.readByte(0x6e));

		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(2);

	}
	protected void stz2_command() {

	}
	protected void stz3_command() {

	}
	protected void stz4_command() {

	}
	protected void sts_command() {

		//parameters
		int r = this.decodeInstructionParameter('d'); // (bad datasheet)
		int k = this.flash.readWordFromInstructionMemory(this.program_counter+2);

		//operation
		this.sram.writeByte(k, this.sram.readByte(r));

		//debug
		//System.out.println("[sts_command()] wrote 0x" + Integer.toHexString(this.sram.readByte(r) & 0xff)
		//		+ " (" + (this.sram.readByte(r) & 0xff) + ") into offset 0x" 
		//		+ Integer.toHexString(k) + " (" + k + ")");

		//program counter
		this.incrementProgramCounter(2);

		//clock
		this.updateClock(2);

	}
	protected void sts16_command() {

	}
	protected void sub_command() {

		//parameters
		int R,d;
		int Rd = this.sram.readByte(d = this.decodeInstructionParameter('d'));
		int Rr = this.sram.readByte(this.decodeInstructionParameter('r'));

		//operation
		this.sram.writeByte(d, R = Rd - Rr);

		//flags
		this.setStatusFlag(C, (~(Rd&0x80) & (Rr&0x80) | (Rr&0x80) & (R&0x80) | (R&0x80) & ~(Rd&0x80)) == 0x80);		
		this.setStatusFlag(Z, R == 0);
		this.setStatusFlag(N, (R&0x80) == 0x80);
		this.setStatusFlag(V, ((Rd&0x80) & ~(Rr&0x80) & ~(R&0x80) | ~(Rd&0x80) & (Rr&0x80) & (R&0x80)) == 0x80);
		this.setStatusFlag(S, this.getStatusFlag(N) ^ this.getStatusFlag(V));
		this.setStatusFlag(H, (~(Rd&0x8) & (Rr&0x8) | (Rr&0x8) & (R&0x8) | (R&0x8) & ~(Rd&0x8)) == 0x8);
		
		//program counter
		this.incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}
	protected void subi_command() {

		//parameters
		int d, R;
		int Rd = this.sram.readByte(d = this.decodeInstructionParameter('d')+0x10); // 16 <= d <= 31 
		int K = this.decodeInstructionParameter('K'); 								// 0 <= K <= 255

		//Operation
		this.sram.writeByte(d, R = Rd - K);

		//debug
		//System.out.println("[subi_command()] wrote 0x" + Integer.toHexString(this.sram.readByte(d) & 0xff)
		//		+ " (" + this.sram.readByte(d)  + ") into offset 0x" + Integer.toHexString(d)
		//		+ " (" + d + ")");

		//flags
		this.setStatusFlag(C, (~(Rd&0x80) & (K&0x80) | (K&0x80) & (R&0x80) | (R&0x80) & ~(Rd&0x80)) == 0x80);		
		this.setStatusFlag(Z, R == 0);
		this.setStatusFlag(N, (R&0x80) == 0x80);
		this.setStatusFlag(V, ((Rd&0x80) & ~(K&0x80) & ~(R&0x80) | ~(Rd&0x80) & (K&0x80) & (R&0x80)) == 0x80);
		this.setStatusFlag(S, this.getStatusFlag(N) ^ this.getStatusFlag(V));
		this.setStatusFlag(H, (~(Rd&0x8) & (K&0x8) | (K&0x8) & (R&0x8) | (R&0x8) & ~(Rd&0x8)) == 0x8);

		//Program Counter
		incrementProgramCounter(1);

		//clock
		this.updateClock(1);

	}

	protected void swap_command() {

	}
	protected void tst_command() {

	}
	protected void wdr_command() {

	}
	protected void xch_command() {

	}

	////////////////////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

	// Instruction Table
	protected static final Instruction[] Instructions = new Instruction[] {

		// brbc and brne are swapped or brbc will always match instead of brne.

		new Instruction(0, "adc", 0x1C00, 0xFC00, "0001 11rd dddd rrrr", 1),
		new Instruction(1, "add", 0x0C00, 0xFC00, "0000 11rd dddd rrrr", 1),
		new Instruction(2, "adiw", 0x9600, 0xFF00, "1001 0110 KKdd KKKK", 2),
		new Instruction(3, "and", 0x2000, 0xFC00, "0010 00rd dddd rrrr", 1),
		new Instruction(4, "andi", 0x7000, 0xF000, "0111 KKKK dddd KKKK", 1),
		new Instruction(5, "asr", 0x9405, 0xFE0F, "1001 010d dddd 0101", 1),
		//new Instruction(6, "bclr", 0x9488, 0xFF8F, "1001 0100 1sss 1000", 1),
		new Instruction(7, "bld", 0xF800, 0xFE08, "1111 100d dddd 0bbb", 1),
		//new Instruction(8, "brbc", 0xF400, 0xFC00, "1111 01kk kkkk ksss", 1),
		//new Instruction(9, "brbs", 0xF000, 0xFC00, "1111 00kk kkkk ksss", 1),
		new Instruction(10, "brcc", 0xF400, 0xFC07, "1111 01kk kkkk k000", 1),
		new Instruction(11, "brcs", 0xF000, 0xFC07, "1111 00kk kkkk k000", 1),
		new Instruction(12, "break", 0x9598, 0xFFFF, "1001 0101 1001 1000", 1),
		new Instruction(13, "breq", 0xF001, 0xFC07, "1111 00kk kkkk k001", 1),
		new Instruction(14, "brge", 0xF404, 0xFC07, "1111 01kk kkkk k100", 1),
		new Instruction(15, "brhc", 0xF405, 0xFC07, "1111 01kk kkkk k101", 1),
		new Instruction(16, "brhs", 0xF005, 0xFC07, "1111 00kk kkkk k101", 1),
		new Instruction(17, "brid", 0xF407, 0xFC07, "1111 01kk kkkk k111", 1),
		new Instruction(18, "brie", 0xF007, 0xFC07, "1111 00kk kkkk k111", 1),
		new Instruction(19, "brlo", 0xF000, 0xFC07, "1111 00kk kkkk k000", 1),
		new Instruction(20, "brlt", 0xF004, 0xFC07, "1111 00kk kkkk k100", 1),
		new Instruction(21, "brmi", 0xF002, 0xFC07, "1111 00kk kkkk k010", 1),
		new Instruction(22, "brne", 0xF401, 0xFC07, "1111 01kk kkkk k001", 1),
		new Instruction(23, "brpl", 0xF402, 0xFC07, "1111 01kk kkkk k010", 1),
		new Instruction(24, "brsh", 0xF400, 0xFC07, "1111 01kk kkkk k000", 1),
		new Instruction(25, "brtc", 0xF406, 0xFC07, "1111 01kk kkkk k110", 1),
		new Instruction(26, "brts", 0xF006, 0xFC07, "1111 00kk kkkk k110", 1),
		new Instruction(27, "brvc", 0xF403, 0xFC07, "1111 01kk kkkk k011", 1),
		new Instruction(28, "brvs", 0xF003, 0xFC07, "1111 00kk kkkk k011", 1),
		//new Instruction(29, "bset", 0x9408, 0xFF8F, "1001 0100 0sss 1000", 1),
		new Instruction(30, "bst", 0xFA00, 0xFE08, "1111 101d dddd 0bbb", 1),
		new Instruction(31, "call", 0x940E, 0xFE0E, "1001 010k kkkk 111k", 2),
		new Instruction(32, "cbi", 0x9800, 0xFF00, "1001 1000 AAAA Abbb", 1),
		new Instruction(33, "cbr", 0x7000, 0xF000, "0111 KKKK dddd KKKK", 1),
		new Instruction(34, "clc", 0x9488, 0xFFFF, "1001 0100 1000 1000", 1),
		new Instruction(35, "clh", 0x94D8, 0xFFFF, "1001 0100 1101 1000", 1),
		new Instruction(36, "cli", 0x94F8, 0xFFFF, "1001 0100 1111 1000", 1),
		new Instruction(37, "cln", 0x94A8, 0xFFFF, "1001 0100 1010 1000", 1),
		//new Instruction(38, "clr", 0x2400, 0xFC00, "0010 01dd dddd dddd", 1),
		new Instruction(-1, "skip clr", 0xFFFF, 0xFFFF, "", 1),
		new Instruction(39, "cls", 0x94C8, 0xFFFF, "1001 0100 1100 1000", 1),
		new Instruction(40, "clt", 0x94E8, 0xFFFF, "1001 0100 1110 1000", 1),
		new Instruction(41, "clv", 0x94B8, 0xFFFF, "1001 0100 1011 1000", 1),
		new Instruction(42, "clz", 0x9498, 0xFFFF, "1001 0100 1001 1000", 1),
		new Instruction(43, "com", 0x9400, 0xFE0F, "1001 010d dddd 0000", 1),
		new Instruction(44, "cp", 0x1400, 0xFC00, "0001 01rd dddd rrrr", 1),
		new Instruction(45, "cpc", 0x0400, 0xFC00, "0000 01rd dddd rrrr", 1),
		new Instruction(46, "cpi", 0x3000, 0xF000, "0011 KKKK dddd KKKK", 1),
		new Instruction(47, "cpse", 0x1000, 0xFC00, "0001 00rd dddd rrrr", 1),
		new Instruction(48, "dec", 0x940A, 0xFE0F, "1001 010d dddd 1010", 1),
		new Instruction(49, "des", 0x940B, 0xFF0F, "1001 0100 KKKK 1011", 1),
		new Instruction(50, "eicall", 0x9519, 0xFFFF, "1001 0101 0001 1001", 1),
		new Instruction(51, "eijmp", 0x9419, 0xFFFF, "1001 0100 0001 1001", 1),
		new Instruction(52, "elpm1", 0x95D8, 0xFFFF, "1001 0101 1101 1000", 1),
		new Instruction(53, "elpm2", 0x9006, 0xFE0F, "1001 000d dddd 0110", 1),
		new Instruction(54, "elpm3", 0x9007, 0xFE0F, "1001 000d dddd 0111", 1),
		new Instruction(55, "eor", 0x2400, 0xFC00, "0010 01rd dddd rrrr", 1),
		new Instruction(56, "fmul", 0x0308, 0xFF88, "0000 0011 0ddd 1rrr", 1),
		new Instruction(57, "fmuls", 0x0380, 0xFF88, "0000 0011 1ddd 0rrr", 1),
		new Instruction(58, "fmulsu", 0x0388, 0xFF88, "0000 0011 1ddd 1rrr", 1),
		new Instruction(59, "icall", 0x9509, 0xFFFF, "1001 0101 0000 1001", 1),
		new Instruction(60, "ijmp", 0x9409, 0xFFFF, "1001 0100 0000 1001", 1),
		new Instruction(61, "in", 0xB000, 0xF800, "1011 0AAd dddd AAAA", 1),
		new Instruction(62, "inc", 0x9403, 0xFE0F, "1001 010d dddd 0011", 1),
		new Instruction(63, "jmp", 0x940C, 0xFE0E, "1001 010k kkkk 110k", 2),
		new Instruction(64, "lac", 0x9206, 0xFE0F, "1001 001r rrrr 0110", 1),
		new Instruction(65, "las", 0x9205, 0xFE0F, "1001 001r rrrr 0101", 1),
		new Instruction(66, "lat", 0x9207, 0xFE0F, "1001 001r rrrr 0111", 1),
		new Instruction(67, "ldx1", 0x900C, 0xFE0F, "1001 000d dddd 1100", 1),
		new Instruction(68, "ldx2", 0x900D, 0xFE0F, "1001 000d dddd 1101", 1),
		new Instruction(69, "ldx3", 0x900E, 0xFE0F, "1001 000d dddd 1110", 1),
		new Instruction(70, "ldy1", 0x8008, 0xFE0F, "1000 000d dddd 1000", 1),
		new Instruction(71, "ldy2", 0x9009, 0xFE0F, "1001 000d dddd 1001", 1),
		new Instruction(72, "ldy3", 0x900A, 0xFE0F, "1001 000d dddd 1010", 1),
		new Instruction(73, "ldy4", 0x8008, 0xD208, "10q0 qq0d dddd 1qqq", 1),
		new Instruction(74, "ldz1", 0x8000, 0xFE0F, "1000 000d dddd 0000", 1),
		new Instruction(75, "ldz2", 0x9001, 0xFE0F, "1001 000d dddd 0001", 1),
		new Instruction(76, "ldz3", 0x9002, 0xFE0F, "1001 000d dddd 0010", 1),
		new Instruction(77, "ldz4", 0x8000, 0xD208, "10q0 qq0d dddd 0qqq", 1),
		new Instruction(78, "ldi", 0xE000, 0xF000, "1110 KKKK dddd KKKK", 1),

		new Instruction(137, "sts", 0x9200, 0xFE0F, "1001 001d dddd 0000", 2),

		new Instruction(79, "lds", 0x9000, 0xFC0F, "1001 000d dddd 0000", 2),
		new Instruction(80, "lds16", 0xA000, 0xF800, "1010 0kkk dddd kkkk", 1),
		new Instruction(81, "lpm1", 0x95C8, 0xFFFF, "1001 0101 1100 1000", 1),
		new Instruction(82, "lpm2", 0x9004, 0xFE0F, "1001 000d dddd 0100", 1),
		new Instruction(83, "lpm3", 0x9005, 0xFE0F, "1001 000d dddd 0101", 1),
		new Instruction(84, "lsl", 0x0C00, 0xFC00, "0000 11dd dddd dddd", 1),
		new Instruction(85, "lsr", 0x9406, 0xFE0F, "1001 010d dddd 0110", 1),
		new Instruction(86, "mov", 0x2C00, 0xFC00, "0010 11rd dddd rrrr", 1),
		new Instruction(87, "movw", 0x0100, 0xFF00, "0000 0001 dddd rrrr", 1),
		new Instruction(88, "mul", 0x9C00, 0xFC00, "1001 11rd dddd rrrr", 1),
		new Instruction(89, "muls", 0x0200, 0xFF00, "0000 0010 dddd rrrr", 1),
		new Instruction(90, "mulsu", 0x0300, 0xFF88, "0000 0011 0ddd 0rrr", 1),
		new Instruction(91, "neg", 0x9401, 0xFE0F, "1001 010d dddd 0001", 1),
		new Instruction(92, "nop", 0x0000, 0xFFFF, "0000 0000 0000 0000", 1),
		new Instruction(93, "or", 0x2800, 0xFC00, "0010 10rd dddd rrrr", 1),
		new Instruction(94, "ori", 0x6000, 0xF000, "0110 KKKK dddd KKKK", 1),
		new Instruction(95, "out", 0xB800, 0xF800, "1011 1AAr rrrr AAAA", 1),
		new Instruction(96, "pop", 0x900F, 0xFE0F, "1001 000d dddd 1111", 1),
		// new Instruction(97, "push", 0x920F, 0xFE0F, "1001 001d dddd 1111", 1),
		new Instruction(97, "push", 0x920F, 0xFE0F, "1001 001r rrrr 1111", 1),
		new Instruction(98, "rcall", 0xD000, 0xF000, "1101 kkkk kkkk kkkk", 1),
		new Instruction(99, "ret", 0x9508, 0xFFFF, "1001 0101 0000 1000", 1),
		new Instruction(100, "reti", 0x9518, 0xFFFF, "1001 0101 0001 1000", 1),
		new Instruction(101, "rjmp", 0xC000, 0xF000, "1100 kkkk kkkk kkkk", 1),
		new Instruction(102, "rol", 0x1C00, 0xFC00, "0001 11dd dddd dddd", 1),
		new Instruction(103, "ror", 0x9407, 0xFE0F, "1001 010d dddd 0111", 1),
		new Instruction(104, "sbc", 0x0800, 0xFC00, "0000 10rd dddd rrrr", 1),
		new Instruction(105, "sbci", 0x4000, 0xF000, "0100 KKKK dddd KKKK", 1),
		new Instruction(106, "sbi", 0x9A00, 0xFF00, "1001 1010 AAAA Abbb", 1),
		new Instruction(107, "sbic", 0x9900, 0xFF00, "1001 1001 AAAA Abbb", 1),
		new Instruction(108, "sbis", 0x9B00, 0xFF00, "1001 1011 AAAA Abbb", 1),
		new Instruction(109, "sbiw", 0x9700, 0xFF00, "1001 0111 KKdd KKKK", 1),
		new Instruction(110, "sbr", 0x6000, 0xF000, "0110 KKKK dddd KKKK", 1),
		new Instruction(111, "sbrc", 0xFC00, 0xFE08, "1111 110r rrrr 0bbb", 1),
		new Instruction(112, "sbrs", 0xFE00, 0xFE08, "1111 111r rrrr 0bbb", 1),
		new Instruction(113, "sec", 0x9408, 0xFFFF, "1001 0100 0000 1000", 1),
		new Instruction(114, "seh", 0x9458, 0xFFFF, "1001 0100 0101 1000", 1),
		new Instruction(115, "sei", 0x9478, 0xFFFF, "1001 0100 0111 1000", 1),
		new Instruction(116, "sen", 0x9428, 0xFFFF, "1001 0100 0010 1000", 1),
		new Instruction(117, "ser", 0xEF0F, 0xFF0F, "1110 1111 dddd 1111", 1),
		new Instruction(118, "ses", 0x9448, 0xFFFF, "1001 0100 0100 1000", 1),
		new Instruction(119, "set", 0x9468, 0xFFFF, "1001 0100 0110 1000", 1),
		new Instruction(120, "sev", 0x9438, 0xFFFF, "1001 0100 0011 1000", 1),
		new Instruction(121, "sez", 0x9418, 0xFFFF, "1001 0100 0001 1000", 1),
		new Instruction(122, "sleep", 0x9588, 0xFFFF, "1001 0101 1000 1000", 1),
		new Instruction(123, "spm", 0x95E8, 0xFFFF, "1001 0101 1110 1000", 1),
		new Instruction(126, "stx1", 0x920C, 0xFE0F, "1001 001r rrrr 1100", 1),
		new Instruction(127, "stx2", 0x920D, 0xFE0F, "1001 001r rrrr 1101", 1),
		new Instruction(128, "stx3", 0x920E, 0xFE0F, "1001 001r rrrr 1110", 1),
		new Instruction(129, "sty1", 0x9208, 0xFE0F, "1000 001r rrrr 1000", 1),
		new Instruction(130, "sty2", 0x9209, 0xFE0F, "1001 001r rrrr 1001", 1),
		new Instruction(131, "sty3", 0x900A, 0xFE0F, "1001 001r rrrr 1010", 1),
		new Instruction(132, "sty4", 0x8208, 0xD208, "10q0 qq1r rrrr 1qqq", 1),
		new Instruction(133, "stz1", 0x8200, 0xFE0F, "1000 001r rrrr 0000", 1),
		new Instruction(134, "stz2", 0x9201, 0xFE0F, "1001 001r rrrr 0001", 1),
		new Instruction(135, "stz3", 0x9202, 0xFE0F, "1001 001r rrrr 0010", 1),
		new Instruction(136, "stz4", 0x8200, 0xD208, "10q0 qq1r rrrr 0qqq", 1),

		new Instruction(138, "sts16", 0xA800, 0xF800, "1010 1kkk dddd kkkk", 1),
		new Instruction(139, "sub", 0x1800, 0xFC00, "0001 10rd dddd rrrr", 1),
		new Instruction(140, "subi", 0x5000, 0xF000, "0101 KKKK dddd KKKK", 1),
		new Instruction(141, "swap", 0x9402, 0xFE0F, "1001 010d dddd 0010", 1),
		new Instruction(142, "tst", 0x2000, 0xFC00, "0010 00dd dddd dddd", 1),
		new Instruction(143, "wdr", 0x95A8, 0xFFFF, "1001 0101 1010 1000", 1),
		new Instruction(145, "xch", 0x9204, 0xFE0F, "1001 001r rrrr 0100", 1)
	};


	protected void callInstruction() {

		switch (this.current_instruction_id) {

		case 0: adc_command(); break;
		case 1: add_command(); break;
		case 2: adiw_command(); break;
		case 3: and_command(); break;
		case 4: andi_command(); break;
		case 5: asr_command(); break;
		case 6: bclr_command(); break;
		case 7: bld_command(); break;
		case 8: brbc_command(); break;
		case 9: brbs_command(); break;
		case 10: brcc_command(); break;
		case 11: brcs_command(); break;
		case 12: break_command(); break;
		case 13: breq_command(); break;
		case 14: brge_command(); break;
		case 15: brhc_command(); break;
		case 16: brhs_command(); break;
		case 17: brid_command(); break;
		case 18: brie_command(); break;
		case 19: brlo_command(); break;
		case 20: brlt_command(); break;
		case 21: brmi_command(); break;
		case 22: brne_command(); break;
		case 23: brpl_command(); break;
		case 24: brsh_command(); break;
		case 25: brtc_command(); break;
		case 26: brts_command(); break;
		case 27: brvc_command(); break;
		case 28: brvs_command(); break;
		case 29: bset_command(); break;
		case 30: bst_command(); break;
		case 31: call_command(); break;
		case 32: cbi_command(); break;
		case 33: cbr_command(); break;
		case 34: clc_command(); break;
		case 35: clh_command(); break;
		case 36: cli_command(); break;
		case 37: cln_command(); break;
		case 38: clr_command(); break;
		case 39: cls_command(); break;
		case 40: clt_command(); break;
		case 41: clv_command(); break;
		case 42: clz_command(); break;
		case 43: com_command(); break;
		case 44: cp_command(); break;
		case 45: cpc_command(); break;
		case 46: cpi_command(); break;
		case 47: cpse_command(); break;
		case 48: dec_command(); break;
		case 49: des_command(); break;
		case 50: eicall_command(); break;
		case 51: eijmp_command(); break;
		case 52: elpm1_command(); break;
		case 53: elpm2_command(); break;
		case 54: elpm3_command(); break;
		case 55: eor_command(); break;
		case 56: fmul_command(); break;
		case 57: fmuls_command(); break;
		case 58: fmulsu_command(); break;
		case 59: icall_command(); break;
		case 60: ijmp_command(); break;
		case 61: in_command(); break;
		case 62: inc_command(); break;
		case 63: jmp_command(); break;
		case 64: lac_command(); break;
		case 65: las_command(); break;
		case 66: lat_command(); break;
		case 67: ldx1_command(); break;
		case 68: ldx2_command(); break;
		case 69: ldx3_command(); break;
		case 70: ldy1_command(); break;
		case 71: ldy2_command(); break;
		case 72: ldy3_command(); break;
		case 73: ldy4_command(); break;
		case 74: ldz1_command(); break;
		case 75: ldz2_command(); break;
		case 76: ldz3_command(); break;
		case 77: ldz4_command(); break;
		case 78: ldi_command(); break;
		case 79: lds_command(); break;
		case 80: lds16_command(); break;
		case 81: lpm1_command(); break;
		case 82: lpm2_command(); break;
		case 83: lpm3_command(); break;
		case 84: lsl_command(); break;
		case 85: lsr_command(); break;
		case 86: mov_command(); break;
		case 87: movw_command(); break;
		case 88: mul_command(); break;
		case 89: muls_command(); break;
		case 90: mulsu_command(); break;
		case 91: neg_command(); break;
		case 92: nop_command(); break;
		case 93: or_command(); break;
		case 94: ori_command(); break;
		case 95: out_command(); break;
		case 96: pop_command(); break;
		case 97: push_command(); break;
		case 98: rcall_command(); break;
		case 99: ret_command(); break;
		case 100: reti_command(); break;
		case 101: rjmp_command(); break;
		case 102: rol_command(); break;
		case 103: ror_command(); break;
		case 104: sbc_command(); break;
		case 105: sbci_command(); break;
		case 106: sbi_command(); break;
		case 107: sbic_command(); break;
		case 108: sbis_command(); break;
		case 109: sbiw_command(); break;
		case 110: sbr_command(); break;
		case 111: sbrc_command(); break;
		case 112: sbrs_command(); break;
		case 113: sec_command(); break;
		case 114: seh_command(); break;
		case 115: sei_command(); break;
		case 116: sen_command(); break;
		case 117: ser_command(); break;
		case 118: ses_command(); break;
		case 119: set_command(); break;
		case 120: sev_command(); break;
		case 121: sez_command(); break;
		case 122: sleep_command(); break;
		case 123: spm_command(); break;
		case 126: stx1_command(); break;
		case 127: stx2_command(); break;
		case 128: stx3_command(); break;
		case 129: sty1_command(); break;
		case 130: sty2_command(); break;
		case 131: sty3_command(); break;
		case 132: sty4_command(); break;
		case 133: stz1_command(); break;
		case 134: stz2_command(); break;
		case 135: stz3_command(); break;
		case 136: stz4_command(); break;
		case 137: sts_command(); break;
		case 138: sts16_command(); break;
		case 139: sub_command(); break;
		case 140: subi_command(); break;
		case 141: swap_command(); break;
		case 142: tst_command(); break;
		case 143: wdr_command(); break;
		case 145: xch_command(); break;

		}
	}
}