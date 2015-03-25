package com.cs5300.pj1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer implements Runnable{
	protected DatagramSocket rpcSocket = null;
	protected boolean listen = true;
	
	public UDPServer(int port) throws IOException {
        super();
        rpcSocket = new DatagramSocket(port);
        System.out.println("=======================================");
        System.out.println("    UDP server started at port " + port);
        System.out.println("=======================================");
    }

	@Override
	public void run() {
		while(listen) {
	        try {
	        	byte[] inBuf = new byte[1024];
	    		byte[] outBuf = null;
	    		DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
		        rpcSocket.receive(recvPkt);
		        
		        InetAddress IPAddress = recvPkt.getAddress();
		        int returnPort = recvPkt.getPort();
		        String recv_string = new String(recvPkt.getData());
		        String[] recv_string_token = recv_string.split("_");
		        int operationCode = Integer.parseInt(recv_string_token[1]);
		        
		        switch (operationCode) {
					case 0: // sessionRead
						// accepts call arguments and returns call results
						outBuf = sessionRead(recvPkt.getData(), recvPkt.getLength());
						break;
					
					case 1: // sessionWrite
						outBuf = sessionWrite(recvPkt.getData(), recvPkt.getLength());
						break;
					
					case 2: // exchangeView
						outBuf = exchangeView(recvPkt.getData(), recvPkt.getLength());
						break;
						
					default: // error occur
						System.out.println("receive unknown operation!!!!");
						break;
				}
		        
		        DatagramPacket sendPacket = new DatagramPacket(outBuf, outBuf.length, IPAddress, returnPort);
		        rpcSocket.send(sendPacket);
	        } catch (Exception e) {
	        	e.printStackTrace();
                listen = false;
	        }
        }
		rpcSocket.close();
	}
	
	public byte[] sessionRead(byte[] pktData, int plyLen) {
		
		return null;
	}
	
	public byte[] sessionWrite(byte[] pktData, int plyLen) {
		
		return null;
	}

	public byte[] exchangeView(byte[] pktData, int plyLen) {
	
		return null;
	}
}