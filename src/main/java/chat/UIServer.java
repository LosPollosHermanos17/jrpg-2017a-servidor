package chat;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Dimension;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class UIServer {

	private JFrame frame;
	private JButton btnStop;
	private JButton btnStart;
	private JTextArea txtLogs;
	private Server server;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIServer window = new UIServer();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	public UIServer() {
		initialize();
		initializeEvents();
	}

	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 50));
		frame.getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(null);

		btnStart = new JButton("Start");
		btnStart.setBounds(90, 16, 89, 23);
		panel.add(btnStart);

		btnStop = new JButton("Stop");
		btnStop.setEnabled(false);
		btnStop.setBounds(239, 16, 89, 23);
		panel.add(btnStop);

		JScrollPane scrollPane = new JScrollPane();
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

		txtLogs = new JTextArea();
		scrollPane.setViewportView(txtLogs);
	}

	private void initializeEvents() {
		btnStart.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onStartClick(e);
			}
		});

		btnStop.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onStopClick(e);
			}
		});
	}

	private void onStartClick(MouseEvent e) {
		try {
			if (server == null) {
				log("Creando servidor en puerto " + 1001);
				server = new Server(this, 1001);
			}
			log("Iniciando servidor...");
			server.start();
			btnStart.setEnabled(false);
			btnStop.setEnabled(true);
		} catch (IOException ex) {
			log("Excepción en el servidor:" + ex.toString());
		}
	}

	private void onStopClick(MouseEvent e) {
		if(server!= null)
		{
			server.stop();
			server = null;
		}
		btnStart.setEnabled(true);
		btnStop.setEnabled(false);
	}

	public void log(String text) {
		txtLogs.setText(txtLogs.getText() + text + "\n");
	}

}
