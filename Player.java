import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;

import javax.swing.JOptionPane;

public class Player implements Runnable {
	private Thread t = new Thread(this);
	private Socket socket;
	private BufferedReader reader;
	private DZServer server;
	private String host;
	private Writer writer;
	private String username;

	public Player(Socket socket, DZServer server) {
		this.socket = socket;
		this.server = server;
		this.host = socket.getInetAddress().getHostName();
		t.start();
	}

	public void run() {

		try {
			writer = new PrintWriter(socket.getOutputStream(), true);
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			String msg;
			while ((msg = reader.readLine()) != null) {
				// server.print(host+ " BROADCASTS: "+msg+"\n");
				// server.broadcast(msg);
				if (!msg.equals("logout"))
					interpret(msg);
				else
					break;
				msg = "";
			}
			server.print(username + " disconnected");
			server.removePlayer(this, username);
			writer.close();
			reader.close();
			socket.close();
			System.out.println("avslutad");
			// server.print(host+" DISCONNECTED\n");
		} catch (IOException e) {
			server.print(username + " disconnected rather brutally");
			server.removePlayer(this, username);
		}
		// server.killHandler(this);
	}

	public void interpret(String msg) {
		System.out.println("Server receiving: " + msg);

		String[] cmd = msg.split("x");
		System.out.println("Server spliting: " + cmd[0]);
		switch (cmd[0]) {
		case "init":
			if(server.checkUsername(cmd[1])){
			username = cmd[1];
			server.addPlayer(this);
			}else{
				try {
					writer.write("newusernamex\n");
					writer.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;
		case "challenge":
			server.challenge(this, cmd[1]);
			break;
		case "accept":			
			server.sendTo(cmd[1],"acceptedx"+username+"x"+cmd[2]+"x"+socket.getInetAddress().getHostAddress()+"x"+username);
			break;
		case "decline":
			server.sendTo(cmd[1],"declinedx"+username);			
			break;
		case "game":
			server.sendTo(cmd[1],"gamex"+username+"x"+cmd[2]+"x"+socket.getInetAddress().getHostAddress()+"x"+username);
			break;
		case "remove":
			server.removePlayer(this, username);
			break;
		case "back":
			server.addPlayer(this);
			break;
		}

	}

	public Socket getSocket() {
		return socket;
	}

	public String getHost() {
		return host;
	}

	public String getUsername() {
		return username;
	}

	public Writer getWriter() {
		return writer;
	}

	public boolean equals(Object o) {
		if (o instanceof Player) {
			Player p = (Player) o;
			if (p.getUsername().equals(this.username))
				return true;
		}
		return false;
	}
}
