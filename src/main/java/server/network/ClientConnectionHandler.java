package server.network;

import shared.request.BaseRequest;
import shared.response.BaseResponse;

import java.io.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnectionHandler implements  Runnable{
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientConnectionHandler(Socket socket)
    {
        this.clientSocket = socket;
    }

    public void run()
    {
        try
        {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            // Đăng kí tạm thời khách có địa chỉ IP (Lúc sau có userId thì thay đổi) đang đuươợc this phục vụ
            RealtimePushServer.registerUser(clientSocket.getInetAddress().toString(), this);
            while (!clientSocket.isClosed())
            {
                readRequest();
            }
        }
        catch (Exception e)
        {
            System.out.println("Client ngắt kết nối: " + e.getMessage());
        } finally
        {
            try
            {
                if (clientSocket != null) clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void readRequest() throws IOException, ClassNotFoundException
    {
            BaseRequest request = (BaseRequest) in.readObject();
            if (request != null)
            {
                dispatch(request);
            }


    }
    public void dispatch(BaseRequest request) {
        // 1. Gọi Router để xử lý và lấy kết quả trả về
        BaseResponse response = RequestRouter.route(request);

        // 2. Gửi kết quả ngược lại cho Client
        sendResponse(response);
    }
    public void sendResponse(Object response)
    {
        try {
            out.writeObject(response);
            out.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
