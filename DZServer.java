import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.*;
/**
 * Server class mostly checking git with this comment
 * @author Klas McDie
 *
 */
public class DZServer extends JFrame {
	private static int port = 2000;
	private JTextArea area = new JTextArea();
	private JScrollPane scroll = new JScrollPane(area);
	private ServerSocket socket;
	// private String host;
	private CopyOnWriteArrayList<Player> players = new CopyOnWriteArrayList<>();

	public static void main(String[] args) {
		if (args.length == 1) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Ogiltig Port", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		new DZServer();
	}

	public DZServer() {
		try {
			socket = new ServerSocket(port);
			// host = socket.getInetAddress().getLocalHost().getHostName();
			setServerGraphics();
			while (true) {
				Socket newplayersocket = socket.accept();
				print(newplayersocket.getInetAddress().getHostName()
						+ " CONNECTED");
				// broadcast(newClient.getInetAddress().getHostName()+" CONNECTED");
				new Player(newplayersocket, this);
				// clients.add(client);
				// setTitle(clients.size() +
				// " client(s) conntected to DangerZone @"+host+":"+port);
			}
		} catch (IOException e) {

		}

	}

	synchronized public void removePlayer(Player p, String username) {
		boolean removed = players.remove(p);
		if (removed)
			broadcast("updatex" + username + "x");

	}

	synchronized public boolean checkUsername(String username) {
		for (Player p : players) {
			if (p.getUsername().equals(username))
				return false;
		}
		return true;
	}

	synchronized public void broadcast(String msg) {
		for (Player p : players) {
			try {
				p.getWriter().write(msg + "\n");
				p.getWriter().flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void setServerGraphics() {
		add(scroll);
		setSize(400, 200);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public void addPlayer(Player player) {
		try {
			String avplayers = "eistingx";
			synchronized (this) {
				for (Player p : players) {
					avplayers += p.getUsername() + "x";
					p.getWriter().write(
							"addplayerx" + player.getUsername() + "x" + "\n");
					System.out.println("Server sending: " + "addplayerx"
							+ player.getUsername() + "x");
					p.getWriter().flush();
				}
			}
			player.getWriter().write(avplayers + "\n");
			player.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		players.add(player);
	}

	public void print(String text) {
		area.append(text+"\n");
	}

	public void challenge(Player challenging, String target) {
		print(challenging.getUsername() + " challenges " + target);
		Player targetplayer = null;
		synchronized (this) {
			for (Player p : players) {
				if (p.getUsername().equals(target))
					targetplayer = p;

			}
		}
		try {
			targetplayer.getWriter().write(
					"challengedx" + challenging.getUsername() + "x\n");
			System.out.print("Server sending challengedx"
					+ challenging.getUsername() + "x");
			targetplayer.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendTo(String receiver, String msg) {
		Player targetplayer = null;
		synchronized (this) {
			for (Player p : players) {
				if (p.getUsername().equals(receiver))
					targetplayer = p;

			}
		}
		try {
			targetplayer.getWriter().write(msg + "x\n");
			targetplayer.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
