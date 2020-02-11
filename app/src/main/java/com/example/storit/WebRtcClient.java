package com.example.storit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import androidx.annotation.Nullable;


public class WebRtcClient {
    // some unique identifier
    private final static String TAG = WebRtcClient.class.getCanonicalName();
    // maximum number of peers
    private final static int MAX_PEER = 2;
    private boolean[] endPoints = new boolean[MAX_PEER];
    // sounds like its similar to angular factory
    // which connected the events to data
    private PeerConnectionFactory factory;
    // data channel
    //public static DataChannel localDataChannel;
    //public static DataChannel remoteDataChannel;
//    public static ArrayList<DataChannel> dataChannelArrayList = new ArrayList<DataChannel>();
//    public static ArrayList<DataChannel> remoteChannnelList = new ArrayList<DataChannel>();;
    // stores the peers
    private HashMap<String, Peer> peers = new HashMap<>();
    private HashMap<String, byte[]> downloadChunks = new HashMap<>();
    private HashMap<String, Integer> downloadIndex = new HashMap<>();
    // stores all the servers
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
    //private PeerConnectionParameters pcParams;
    private MediaConstraints pcConstraints = new MediaConstraints();
    private MediaStream localMS;
    private VideoSource videoSource;
    private Socket client;

    Button sendButton;
    EditText textInput;
    ImageView image;
    TextView remoteText;

    boolean receivingFile;
    boolean completedReceiving;
    int incomingFileSize;
    int currentIndexPointer;
    byte[] imageFileBytes;
    String type = "";
    String requestType = "";
    Uri uri;
    int fileId;
    ArrayList<Integer> fileSizes = new ArrayList<Integer>();
    ArrayList<Integer> receivingFileSizes = new ArrayList<Integer>();

    @Nullable Intent data;

    public static final int CHUNK_SIZE = 64000;
    ArrayList<byte[]> chunks = new ArrayList<byte[]>();


    protected Menu context;


    // this is an interface that is used to organize commands
    private interface Command {
        void execute(String peerId, JSONObject payload) throws JSONException;
    }


    // when receiving init
    // create generate local sdp(which happens when u create peer instance)
    // send local sdp to peer
    private class CreateOfferCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Peer peer = peers.get(peerId);
            peer.pc.createOffer(peer, pcConstraints);
        }
    }


    // on offer
    // set remote sdp which u received from other peer
    // generate local sdp (happens when u add new peer instance)
    // send local sdp to peer
    private class CreateAnswerCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Peer peer = peers.get(peerId);
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    payload.getString("sdp")
            );
            peer.pc.setRemoteDescription(peer, sdp);
            peer.pc.createAnswer(peer, pcConstraints);
        }
    }

    // when receiving answer
    // set remote sdp
    private class SetRemoteSDPCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Peer peer = peers.get(peerId);
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    payload.getString("sdp")
            );
            peer.pc.setRemoteDescription(peer, sdp);
        }
    }


    // add an ice server candidate
    private class AddIceCandidateCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            PeerConnection pc = peers.get(peerId).pc;
            if (pc.getRemoteDescription() != null) {
                IceCandidate candidate = new IceCandidate(
                        payload.getString("id"),
                        payload.getInt("label"),
                        payload.getString("candidate")
                );
                pc.addIceCandidate(candidate);
            }
        }
    }


    // look u can send whatever u want via the sockets
    // u just need to make sure the sockets are set up appropriately
    /**
     * Send a message through the signaling server
     *
     * @parao      id of recipient
     * @param type    type of message
     * @param payload payload of message
     * @throws JSONException
     */
    public void sendMessage(String to, String type, JSONObject payload, String requestType, int orderIndex) throws JSONException {
        JSONObject message = new JSONObject();
        message.put("to", to);
        message.put("type", type);
        message.put("payload", payload);
        message.put("requestType", requestType);
        message.put("order", orderIndex);
        client.emit("message", message);
    }
    public void uploadRequest(int orderIndex, DataChannel dc) {
        sendImage(chunks.get(orderIndex), dc);
        if(orderIndex == chunks.size() - 1) {
            context.doneUploading();
        }
    }
    public void sendImage(byte[] file,  DataChannel dc) {
        int size = file.length;
        int numberOfChunks = size / CHUNK_SIZE;
        ByteBuffer meta = stringToByteBuffer("-i" + size, Charset.defaultCharset());
        //WebRtcClient.localDataChannel.send(new DataChannel.Buffer(meta, false));
        dc.send(new DataChannel.Buffer(meta, false));

        for (int i = 0; i < numberOfChunks; i++) {
            ByteBuffer wrap = ByteBuffer.wrap(file, i * CHUNK_SIZE, CHUNK_SIZE);
            //WebRtcClient.localDataChannel.send(new DataChannel.Buffer(wrap, false));
            dc.send(new DataChannel.Buffer(wrap, false));
        }
        int remainder = size % CHUNK_SIZE;
        if (remainder > 0) {
            ByteBuffer wrap = ByteBuffer.wrap(file, numberOfChunks * CHUNK_SIZE, remainder);
            //WebRtcClient.localDataChannel.send(new DataChannel.Buffer(wrap, false));
            dc.send(new DataChannel.Buffer(wrap, false));
        }
    }

    public void downloadRequest(DataChannel dc) {
        ByteBuffer meta = stringToByteBuffer("-d" , Charset.defaultCharset());
        //WebRtcClient.localDataChannel.send(new DataChannel.Buffer(meta, false));
        dc.send(new DataChannel.Buffer(meta, false));
    }

    // receive message from socket
    private void readIncomingMessage(ByteBuffer buffer, DataChannel sendingChannel, String id) {
        byte[] bytes;
        if (buffer.hasArray()) {
            bytes = buffer.array();
        } else {
            bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
        }
        if (!receivingFile) {
            String firstMessage = new String(bytes, Charset.defaultCharset());
            String type = firstMessage.substring(0, 2);

            if (type.equals("-i")) {
                incomingFileSize = Integer.parseInt(firstMessage.substring(2, firstMessage.length()));
                imageFileBytes = new byte[incomingFileSize];
                Log.d(TAG, "readIncomingMessage: incoming file size " + incomingFileSize);
                receivingFile = true;
            } else if (type.equals("-d")) {
                Log.d(TAG, "-dreceived request of type download");
//                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                image = (ImageView) context.layout.findViewById(R.id.dialogImage);
                Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                byte[] imageInByte = baos.toByteArray();
                sendImage(imageInByte,sendingChannel);
            }
        } else {
            for (byte b : bytes) {
                imageFileBytes[currentIndexPointer++] = b;
            }
            if (currentIndexPointer == incomingFileSize) {
                Log.d(TAG, "readIncomingMessage: received all bytes");
                Bitmap bmp = BitmapFactory.decodeByteArray(imageFileBytes, 0, imageFileBytes.length);
                receivingFile = false;
                currentIndexPointer = 0;
//                disconnect();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        context.showImage(bmp);
                    }
                });
                for(String key : peers.keySet()) {
                    removePeer(peers.get(key).id);
                }
            }
        }
    }
    // receive message from socket
    private void readIncomingMessageForClient(ByteBuffer buffer, DataChannel sendingChannel, String id) {
        byte[] bytes;
        if (buffer.hasArray()) {
            bytes = buffer.array();
        } else {
            bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
        }
        if (!peers.get(id).receivingFile) {
            String firstMessage = new String(bytes, Charset.defaultCharset());
            String type = firstMessage.substring(0, 2);

            if (type.equals("-i")) {
                peers.get(id).incomingFileSize = Integer.parseInt(firstMessage.substring(2, firstMessage.length()));
                peers.get(id).imageFileBytes = new byte[peers.get(id).incomingFileSize];
                Log.d(TAG, "readIncomingMessage: incoming file size " + peers.get(id).incomingFileSize);
                peers.get(id).receivingFile = true;
            }
        } else {
            for (byte b : bytes) {
                peers.get(id).imageFileBytes[peers.get(id).currentIndexPointer++] = b;
            }
            if (peers.get(id).currentIndexPointer == peers.get(id).incomingFileSize) {
                Log.d(TAG, "readIncomingMessage: received all bytes");
                Bitmap bmp = BitmapFactory.decodeByteArray(peers.get(id).imageFileBytes, 0, peers.get(id).imageFileBytes.length);
                peers.get(id).receivingFile = false;
                peers.get(id).currentIndexPointer = 0;
                peers.get(id).completelyReceived = true;
//                disconnect();
//                context.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        image.setImageBitmap(bmp);
//                    }
//                });
                Iterator<String> itr = peers.keySet().iterator();
                while (itr.hasNext()) {
                    if(!peers.get(itr.next()).completelyReceived) {
                        break;
                    } else if(!itr.hasNext()){
                        mergePhoto();
                        completedReceiving = true;
                    }
                }
//                removePeer(id);
//
//
//                removePeer(id);
            }
        }
    }
    public void mergePhoto() {
        Bitmap[] bmpList = new Bitmap[2];
        int i = 0;
        for(String key: peers.keySet()) {
            bmpList[i] = BitmapFactory.decodeByteArray(peers.get(key).imageFileBytes, 0, peers.get(key).imageFileBytes.length);
            i++;
        }
        Bitmap combinedbmp = combineImages(bmpList[0], bmpList[1]);
            context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                context.showImage(combinedbmp);
            }
        });

    }
    public Bitmap combineImages(Bitmap c, Bitmap s) {
        Bitmap cs = null;

        int width, height = 0;

        if(c.getHeight() > s.getHeight()) {
            height = c.getHeight() + s.getHeight();
            width = c.getWidth();
        } else {
            height = s.getHeight() + s.getHeight();
            width = c.getWidth();
        }

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, 0f, c.getHeight(), null);

        return cs;
    }
    private static ByteBuffer stringToByteBuffer(String msg, Charset charset) {
        return ByteBuffer.wrap(msg.getBytes(charset));
    }


    // a class created to handle messages
    private class MessageHandler {
        private HashMap<String, Command> commandMap;

        // constructor that creates a hashmap and enters all those commands we made above

        private MessageHandler() {
            this.commandMap = new HashMap<>();
            commandMap.put("init", new CreateOfferCommand());
            commandMap.put("offer", new CreateAnswerCommand());
            commandMap.put("answer", new SetRemoteSDPCommand());
            commandMap.put("candidate", new AddIceCandidateCommand());
        }
        // create a listener
        private Emitter.Listener onMessage = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String from = data.getString("from");
                    String type = data.getString("type");
                    String requestType = data.getString("requestType");
                    int orderIndex = data.getInt("order");
                    Log.d(TAG, "the type of message received is " + type);
                    JSONObject payload = null;
                    if (!type.equals("init")) {
                        payload = data.getJSONObject("payload");
                    }
                    // if peer is unknown, try to add him
                    if (!peers.containsKey(from)) {
                        // if MAX_PEER is reach, ignore the call
                        int endPoint = findEndPoint();
                        if (endPoint != MAX_PEER) {
                            Peer peer = addPeer(from, endPoint, requestType, orderIndex);
                            //peer.pc.addStream(localMS);
                            commandMap.get(type).execute(from, payload);
                        }
                    } else {
                        commandMap.get(type).execute(from, payload);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        private Emitter.Listener onId = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String id = (String) args[0];
                Log.d(TAG, "you genius id " + id);
                try {
                    sendMessage(id, "init", null, requestType, -1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        // Steps
        // Divide selected file into chunks
        // I guess store the chunks in a 2d byte array
        // then for each index in the byte array
        // client.emit init
        // right so u also need to inform them of which index you're sending
        //  send firebase file id
        // emit an init with each
        private Emitter.Listener onUploadList = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray socketList = (JSONArray) args[0];
                fileId = (int) args[1];
                // Steps
                // 1 Dele
                // so do two things
                // one is take the socket list and send an init to the requested server
                // we're going to send the requested chunk to that server
                // then the other thing is that u need to send the file id to firebase to update the filestructure

                // now look, u got a list of sockets for each chunk, maybe even the chunk sizes
                // right so divide the file that's in data an


                // shud later pass the number of chunks or like the whole socket list
                // or like a list of how much each chunk should have
                chunks = new ArrayList<byte []>();
                divideData();
                try {

                    for(int i = 0;i < socketList.length();i++) {
                        JSONObject socketObj = (JSONObject) socketList.get(i);
                        String socketId = socketObj.getString("socketId");
                        Log.d("STORIT---", "the socket id is " + socketId);
                        sendMessage(socketId, "init", null, requestType, i);
                    }
                    // lsn u got to update firebase with file id
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        // when u get sockets to download from
        // init conversation with all of them
        private Emitter.Listener onDownloadList = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "on download list triggered");
                JSONArray socketList = (JSONArray) args[0];
                try {
                    Log.d(TAG, "socket convo started with " + socketList.length() + " sockets");
                    for(int i = 0;i < socketList.length();i++) {
                        JSONObject socketObj = (JSONObject) socketList.get(i);
                        String socketId = socketObj.getString("socketId");
                        Log.d("STORIT---", "the socket id is " + socketId);
                        sendMessage(socketId, "init", null, requestType, i);
                    }
                    // lsn u got to update firebase with file id
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        public void divideData() {
//            Uri uri = data.getData();
            String hi = "";
            char[] charArr = new char[1];
            Log.d("STORIT---", uri.toString());
            //using input stream
            try {
//                    File file = new File(uri.toString());
//                    int fileSize = (int) file.length();
//                    FileInputStream fin = new FileInputStream(getActivity().getContentResolver().openFileDescriptor(uri, 'r'));
                // Let's say this bitmap is 300 x 600 pixels

                InputStream in = context.getContentResolver().openInputStream(uri);
//                getActivity().getContentResolver().openInputStream(uri);
                Bitmap originalBm = BitmapFactory.decodeStream(in);
//                    Bitmap originalBm = BitmapFactory.decodeFile(uri.toString());
                Bitmap bm1 = Bitmap.createBitmap(originalBm, 0, 0, originalBm.getWidth(), (originalBm.getHeight() / 2));
                Bitmap bm2 = Bitmap.createBitmap(originalBm, 0, (originalBm.getHeight() / 2), originalBm.getWidth(), (originalBm.getHeight() / 2));
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bm1.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                byte[] byteArray1 = stream.toByteArray();
                chunks.add(stream.toByteArray());
                bm1.recycle();
                ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                bm2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
//                byte[] byteArray2 = stream2.toByteArray();
                chunks.add(stream2.toByteArray());
                bm2.recycle();
                stream.close();
                stream2.close();
//                    FileInputStream fin = (FileInputStream) getActivity().getContentResolver().openInputStream(uri);
//                    //int fileSize = fin.getChannel().size();
//                    //InputStream in = getActivity().getContentResolver().openInputStream(uri);
//                    int fileSize = (int) fin.getChannel().size();
//                    Log.d("WebRtcClient", "sending file of size " + fileSize);
//                    //FileInputStream fin = new FileInputStream(uri.toString());
//
//                    //BufferedInputStream bin=new BufferedInputStream(fin);
//                    int size1 = fileSize / 2;
//                    int size2 = fileSize / 2;
//                    if(fileSize %2 == 1) {
//                        size2 += 1;
//                    }
//                    //size2 += 8;
//                    //byte[] bytesArray = new byte[fileSize];
//                    byte[] bytesArray1 = new byte[size1];
//                    byte[] bytesArray2 = new byte[size2 + 5000];
////                    fin.read(bytesArray);
//                    fin.read(bytesArray1);
//
//                    for(int i = 0; i <5000;i++) {
//                        bytesArray2[i] = bytesArray1[i];
//                    }
//                    fin.read(bytesArray2);
//
////                    fin.read(bytesArray2);
////                    fin.read(bytesArray1, 0, size1);
////                    fin.read(bytesArray2, 0, size2);
//
//                    Log.d("WebRtcClient", "sending file of size " + fileSize);
////                    int i;
////                    while((i=bin.read())!=-1){
////                        charArr[i] = (char)i;
////                    }
////
////                    bin.close();
//                    //client.sendImage(fileSize, bytesArray, 0);
//                client.sendImage(byteArray1.length, byteArray1, 0);
//                client.sendImage(byteArray2.length, byteArray2, 1);
//                    fin.close();
                in.close();
                Log.d("STORIT---", "Done with file splitting");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    // this is the peer class
    private class Peer implements SdpObserver, PeerConnection.Observer {
        private PeerConnection pc;
        private String id;
        private String requestType;
        boolean receivingFile;
        byte[] imageFileBytes;
        int currentIndexPointer;
        int incomingFileSize;
        boolean completelyReceived;
        private int orderIndex;
        private int endPoint;
        DataChannel localDataChannel;
        DataChannel remoteDataChannel;

        // after creating the peer
        // send a message to id
        @Override
        public void onCreateSuccess(final SessionDescription sdp) {
            // TODO: modify sdp to use pcParams prefered codecs
            try {
                Log.d(TAG, "lol:successful creation");
                Log.d(TAG, sdp.type.canonicalForm() + "   " + id);
                JSONObject payload = new JSONObject();
                payload.put("type", sdp.type.canonicalForm());
                payload.put("sdp", sdp.description);
                sendMessage(id, sdp.type.canonicalForm(), payload, requestType, orderIndex);

                pc.setLocalDescription(Peer.this, sdp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSetSuccess() {
        }

        @Override
        public void onCreateFailure(String s) {
        }

        @Override
        public void onSetFailure(String s) {
        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
                removePeer(id);
            }
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        }

        // if u get a new ice candidate
        // send him a message
        @Override
        public void onIceCandidate(final IceCandidate candidate) {
            try {
                JSONObject payload = new JSONObject();
                payload.put("label", candidate.sdpMLineIndex);
                payload.put("id", candidate.sdpMid);
                payload.put("candidate", candidate.sdp);
                sendMessage(id, "candidate", payload, requestType, -1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            Log.d(TAG, "onAddStream " + mediaStream.label());
            // remote streams are displayed from 1 to MAX_PEER (0 is localStream)
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            Log.d(TAG, "onRemoveStream " + mediaStream.label());
            removePeer(id);
        }

        // receiving datachannel
        @Override
        public void onDataChannel(final DataChannel dc) {
            // receiving datachannel
            dc.registerObserver(new DataChannel.Observer() {

                @Override
                public void onStateChange() {
//                    Log.d(TAG, "onStateChange: remote data channel state: " + dc.state().toString());
//                    Log.d(TAG, "the type of datachannel is " + type);
//                    if(dc.state() == DataChannel.State.OPEN && type.equals("serverUpload")) {
//                        Log.d(TAG, "send image is called");
//                        sendImage(orderIndex, dc);
//                    }
//                    if(dc.state() == DataChannel.State.CLOSED) {
//                        removePeer(id);
//                    }
                }
                @Override
                public void onMessage(DataChannel.Buffer buffer) {
                    Log.d(TAG, "onMessage: got message");
                    if(type.equals("server")) {
                        readIncomingMessage(buffer.data, localDataChannel, id);
                    } else if(type.equals("client")) {
                        readIncomingMessageForClient(buffer.data, localDataChannel, id);
                    }
                }
            });
            //WebRtcClient.remoteDataChannel = dc;
            remoteDataChannel = dc;
        }

        @Override
        public void onRenegotiationNeeded() {

        }
        // constructor
        // create a new connection using a factory
        public Peer(String id, int endPoint, String requestType, int orderIndex) {
            Log.d(TAG, "new Peer: " + id + " " + endPoint);
            this.pc = factory.createPeerConnection(iceServers, pcConstraints, this);
            this.id = id;
            this.orderIndex = orderIndex;
            this.requestType = requestType;
            this.endPoint = endPoint;
            localDataChannel = this.pc.createDataChannel("sendDataChannel", new DataChannel.Init());

            localDataChannel.registerObserver(new DataChannel.Observer() {
                // if the state changes
                @Override
                public void onStateChange() {
                    if(localDataChannel.state() == DataChannel.State.OPEN) {
                        if(requestType.equals("serverUpload") && type.equals("client")) {
                            Log.d(TAG, "send image is called");
                            uploadRequest(orderIndex, localDataChannel);
                        } else if(requestType.equals("serverDownload") && type.equals("client")) {
                            Log.d(TAG, "send image is called");
                            downloadRequest(localDataChannel);
                        }
                    }
//                    if(localDataChannel.state() == DataChannel.State.OPEN && type == "serverUpload") {
//                        sendImage(orderIndex);
////                        try {
////                            sendMessage(id, "init", null, "serverUpload");
////                        } catch (JSONException e) {
////                            e.printStackTrace();
////                        }
//                    }
                    //Log.d(TAG, "onStateChange: " + localDataChannel.state().toString());
//                    context.runOnUiThread(() -> {
//                        if (localDataChannel.state() == DataChannel.State.OPEN) {
//                            sendButton.setEnabled(true);
//                        } else {
//                            sendButton.setEnabled(false);
//                        }
//                    });
                }

                @Override
                public void onMessage(DataChannel.Buffer buffer) {
                    readIncomingMessage(buffer.data, localDataChannel, id);
                }
            });
//            Log.d(TAG, "DATA channel size is " + dataChannelArrayList.size());
        }
        public void disconnectPeer() {
            localDataChannel.close();
            remoteDataChannel.close();
            this.pc.dispose();
        }
    }
//    // create a datachannel ovserver
//    private class DcObserver implements DataChannel.Observer {
//
//        @Override
//        public void onMessage(final DataChannel.Buffer buffer) {
//
//            ByteBuffer data = buffer.data;
//            byte[] bytes = new byte[data.remaining()];
//            data.get(bytes);
//            final String command = new String(bytes);
//
//            executor.execute(new Runnable() {
//                public void run() {
//                    events.onReceivedData(command);
//                }
//            });
//
//        }
//
//        @Override
//        public void onStateChange() {
//            Log.d(TAG, "DataChannel: onStateChange: " + dataChannel.state());
//        }
//    }
    private Peer addPeer(String id, int endPoint, String type, int orderIndex) {
        Peer peer = new Peer(id, endPoint, type, orderIndex);
        peers.put(id, peer);

        endPoints[endPoint] = true;
        return peer;
    }

    private void removePeer(String id) {
        Peer peer = peers.get(id);
        if(peer.localDataChannel.state() != DataChannel.State.CLOSED)
            peer.localDataChannel.close();
        if(peer.remoteDataChannel.state() != DataChannel.State.CLOSED)
            peer.remoteDataChannel.close();
        peer.pc.close();
        peers.remove(peer.id);
        endPoints[peer.endPoint] = false;
    }
    // constructor
    public WebRtcClient(String host, Menu _context, String type) {
        context = _context;
        this.type = type;
        PeerConnectionFactory.initializeAndroidGlobals(_context, true, true, false, null);
        factory = new PeerConnectionFactory();
        MessageHandler messageHandler = new MessageHandler();

        try {
            Log.d(TAG, "made it to sockets");
            client = IO.socket(host);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        client.on("id", messageHandler.onId);
        client.on("message", messageHandler.onMessage);
        client.on("uploadList", messageHandler.onUploadList);
        client.on("downloadList", messageHandler.onDownloadList);
        client.connect();

        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("stun:23.21.150.121"));

        iceServers.add(new PeerConnection.IceServer("stun:stun1.l.google.com:19302"));

        iceServers.add(new PeerConnection.IceServer("stun:stun2.l.google.com:19302"));

        iceServers.add(new PeerConnection.IceServer("stun:stun3.l.google.com:19302"));

        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"));
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
//        if (type.equals("server") && requestType.equals("serverDownload")) {
//            image = (ImageView) context.layout.findViewById(R.id.dialogImage);
//        }
//        else
//            image = (ImageView) context.findViewById(R.id.dialogImage);

    }
    public void emitServer(String token, int storageSize, String deviceId) {
        JSONObject message = new JSONObject();

        try {
            message.put("token", token);
            message.put("size", storageSize);
            message.put("deviceId", deviceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        client.emit("addserver", message);
    }
    public void emitUpload(String token,String requestType,  @Nullable Intent data) {
        this.data = data;
        this.requestType = requestType;
        uri = data.getData();
        File f = new File(uri.getPath());
        long size = f.length();
        JSONObject message = new JSONObject();

        try {
            message.put("token", token);
            message.put("size", size);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        client.emit("upload", message);
    }
    public void emitDownload(String token, String requestType, String fileName) {
        Log.d(TAG, "download emitted");
        this.requestType = requestType;
        JSONObject message = new JSONObject();

        try {
            message.put("token", token);
            message.put("fileId", fileId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        client.emit("download", message);
    }


    /**
     * Call this method in Activity.onDestroy()
     */
    public void onDestroy() {

        for (Peer peer : peers.values()) {
            peer.pc.dispose();
        }

        factory.dispose();
        client.disconnect();
        client.close();
    }


    private int findEndPoint() {
        for (int i = 0; i < MAX_PEER; i++) if (!endPoints[i]) return i;
        return MAX_PEER;
    }


}
