package main;

import javax.swing.*;

import arduino.Arduino;
import java.awt.*;
import java.awt.event.*;

import java.util.Random;

public class WorkspaceGUI extends JFrame {
	
	JPanel jp = new JPanel();
	JButton b_step = new JButton("Step");
	JButton b_run = new JButton("Run");
	JButton b_dump = new JButton("Dump Registers");
	JButton b_examine = new JButton("Examine offset");
	JTextField jtf_offset = new JTextField("0x000000");
	JTextField jtf_step = new JTextField("000001");
	JTextArea jta_registers = new JTextArea("Registers [SRAM offsets 0x00 - 0x20]");
	JTextArea jta_instruction = new JTextArea("Instruction Data");
	GridBagLayout gb = new GridBagLayout();
	GridBagConstraints gc = new GridBagConstraints();
	
	Arduino a = new Arduino();
	
	public WorkspaceGUI() {
		init();
	}
	
	
	public void init() {
		
		this.setTitle("Window ID:" + new Random().nextInt(1000));
		
		this.setSize(300,160);
		//this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jp.setLayout(gb);
		
		
		//buttons
		b_step.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				a.step(Integer.parseInt(jtf_step.getText().trim()));
			}
			
		});
		b_dump.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				System.out.println(a.getCpuRegisters());
				System.out.println(a.getXYZPointers());
				System.out.println(a.getCpuFlags() + "\n");
				
				
			}
		});
		
		b_examine.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				
				System.out.println(a.getOffsetValue(jtf_offset.getText()));
				
			}	
		});
		
		b_run.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				a.run();
			}
		});
		
		/*
		gc.anchor = GridBagConstraints.NORTH;
		gc.insets = new Insets(5,5,5,5);
		gc.gridx = 0;
		gc.gridy = 0;
		jp.add(b, gc);
		gc.gridy = 1;
		jp.add(jta_registers, gc);
		gc.gridx=1;
		jp.add(jta_instruction, gc);
		*/
		
		jp.add(b_step, gc);
		gc.gridx = 1;
		jp.add(jtf_step, gc);
		gc.gridx = 0;
		gc.gridy = 1;
		jp.add(b_dump, gc);
		gc.gridy = 2;
		jp.add(b_examine, gc);
		gc.gridx = 1;
		jp.add(jtf_offset, gc);
		gc.gridx = 0;
		gc.gridy = 3;
		jp.add(b_run, gc);
		this.add(jp, BorderLayout.NORTH);
		this.setVisible(true);
	
	}
	
	      
	
}
