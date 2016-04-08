import java.net.*;
import java.util.*;
import java.lang.Thread;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class tcpservice
{       
    // server socket
    private static final int SERVERPORT = 9054; 
    // store all the client socket
    private static List<Socket> mClientList = new ArrayList<Socket>(); 
    // thread pool
    private ExecutorService mExecutorService;  
    // ServerSocket
    private ServerSocket mServerSocket;  
    // ArrayList of user names
    private static ArrayList<String> UserNameList = new ArrayList<String>();
    // constructor
    public static void main(String[] args)
    {
        new tcpservice();
    }
    //
    public tcpservice()
    {
        try
        {
            // server socket port
            mServerSocket = new ServerSocket(SERVERPORT);
            // create a thread pool
            mExecutorService = Executors.newCachedThreadPool();
            System.out.println("start...");
            // temperarily store client socket
            Socket client = null;
            while (true)
            {
                // accept client connection and add to list
                client = mServerSocket.accept(); 
                mClientList.add(client);
                // open a client thread
                mExecutorService.execute(new ThreadServer(client));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }   
    // open a new thread for every client
    static class ThreadServer implements Runnable
    {
        private Socket            mSocket;
        private BufferedReader    mBufferedReader;
        private PrintWriter       mPrintWriter;
        private String            mStrMSG;
        // client specific flag to indicate whether the user name is accepted
        private boolean registered = false;
        // client specific user name
        private String            UserName;
        public ThreadServer(Socket socket) throws IOException
        {
            this.mSocket = socket;
            mBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            mPrintWriter = new PrintWriter(socket.getOutputStream(), true);
    		while(!registered){
				while((mStrMSG = mBufferedReader.readLine()) != null){
                    if(UserNameList.contains(mStrMSG.trim())){
                        mPrintWriter.println("User Name Already Exist\n");
					}else{
                        registered = true;
		                mPrintWriter.println("Welcome to the chat room\n");
                        UserName = mStrMSG.trim();
                        UserNameList.add(UserName);
					}
				}
			}
            mStrMSG = "user("+UserName+"):"+this.mSocket.getInetAddress()+" come total:" + mClientList.size();
            sendMessage();
        }
        public void run()
        {
            try
            {
			while ((mStrMSG = mBufferedReader.readLine()) != null)
                {
                    if (mStrMSG.trim().equals("client_exit"))
                    {
                        // when a client exit
                        mClientList.remove(mSocket);
                        mBufferedReader.close();
                        mPrintWriter.close();
                        mStrMSG = "user("+UserName+"):"+this.mSocket.getInetAddress()+" exit total:" + mClientList.size();
                        mSocket.close();
                        UserNameList.remove(UserName);
                        sendMessage();
                        break;
                    }
                    else
                    {
                        mStrMSG = "user("+UserName+"):"+mSocket.getInetAddress() + ":" + mStrMSG;
                        sendMessage();
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        // broadcast message to all the clients
        private void sendMessage() throws IOException
        {
            System.out.println(mStrMSG);
            for (Socket client : mClientList)
            {
                mPrintWriter = new PrintWriter(client.getOutputStream(), true);
                mPrintWriter.println(mStrMSG);
            }
        }
    }
}