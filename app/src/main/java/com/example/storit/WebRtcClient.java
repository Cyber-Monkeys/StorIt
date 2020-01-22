package com.example.storit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

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

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;


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
    public static ArrayList<DataChannel> dataChannelArrayList = new ArrayList<DataChannel>();
    public static ArrayList<DataChannel> remoteChannnelList = new ArrayList<DataChannel>();;
    // stores the peers
    private HashMap<String, Peer> peers = new HashMap<>();
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
    int incomingFileSize;
    int currentIndexPointer;
    byte[] imageFileBytes;

    public static final int CHUNK_SIZE = 64000;


    protected Menu context;


    private interface Command {
        void execute(String peerId, JSONObject payload) throws JSONException;
    }
    // gets the peer id
    // and connects to it
    private class CreateOfferCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Log.d(TAG, "CreateOfferCommand");
            Peer peer = peers.get(peerId);
            peer.pc.createOffer(peer, pcConstraints);
        }
    }

    // create answer
    private class CreateAnswerCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Log.d(TAG, "CreateAnswerCommand");
            Peer peer = peers.get(peerId);
            Log.d(TAG,payload.getString("type"));
            Log.d(TAG, payload.getString("sdp"));
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    payload.getString("sdp")
            );
            peer.pc.setRemoteDescription(peer, sdp);
            peer.pc.createAnswer(peer, pcConstraints);
        }
    }

    // some signal processing
    private class SetRemoteSDPCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Log.d(TAG, "SetRemoteSDPCommand");
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
            Log.d(TAG, "AddIceCandidateCommand");
            Log.d(TAG, payload.getString("id"));
            Log.d(TAG, payload.getString("label"));
            Log.d(TAG, payload.getString("candidate"));

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
    public void sendMessage(String to, String type, JSONObject payload) throws JSONException {

        JSONObject message = new JSONObject();
        message.put("to", to);
        message.put("type", type);
        message.put("payload", payload);
        client.emit("message", message);
    }
    public void sendImage(int size, byte[] bytes, int dataChannelSelected) {
        int numberOfChunks = size / CHUNK_SIZE;

        ByteBuffer meta = stringToByteBuffer("-i" + size, Charset.defaultCharset());
        //WebRtcClient.localDataChannel.send(new DataChannel.Buffer(meta, false));
        dataChannelArrayList.get(dataChannelSelected).send(new DataChannel.Buffer(meta, false));

        for (int i = 0; i < numberOfChunks; i++) {
            ByteBuffer wrap = ByteBuffer.wrap(bytes, i * CHUNK_SIZE, CHUNK_SIZE);
            //WebRtcClient.localDataChannel.send(new DataChannel.Buffer(wrap, false));
            Log.d(TAG, "" + dataChannelArrayList.size());
            dataChannelArrayList.get(dataChannelSelected).send(new DataChannel.Buffer(wrap, false));
        }
        int remainder = size % CHUNK_SIZE;
        if (remainder > 0) {
            ByteBuffer wrap = ByteBuffer.wrap(bytes, numberOfChunks * CHUNK_SIZE, remainder);
            //WebRtcClient.localDataChannel.send(new DataChannel.Buffer(wrap, false));
            dataChannelArrayList.get(dataChannelSelected).send(new DataChannel.Buffer(wrap, false));
        }
    }

    // receive message from socket
    private void readIncomingMessage(ByteBuffer buffer) {
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
            } else if (type.equals("-s")) {
                context.runOnUiThread(() -> remoteText.setText(firstMessage.substring(2, firstMessage.length())));
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
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        image.setImageBitmap(bmp);
                    }
                });
            }
        }
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
                            Peer peer = addPeer(from, endPoint);
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
                    sendMessage(id, "init", null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }


    // this is the peer class
    private class Peer implements SdpObserver, PeerConnection.Observer {
        private PeerConnection pc;
        private String id;
        private int endPoint;

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
                sendMessage(id, sdp.type.canonicalForm(), payload);
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
                sendMessage(id, "candidate", payload);
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

        @Override
        public void onDataChannel(final DataChannel dc) {
            dc.registerObserver(new DataChannel.Observer() {

                @Override
                public void onStateChange() {
                    Log.d(TAG, "onStateChange: remote data channel state: " + dc.state().toString());
                }

                @Override
                public void onMessage(DataChannel.Buffer buffer) {
                    Log.d(TAG, "onMessage: got message");
                    readIncomingMessage(buffer.data);
                }
            });
            //WebRtcClient.remoteDataChannel = dc;
            remoteChannnelList.add(dc);
        }

        @Override
        public void onRenegotiationNeeded() {

        }
        // constructor
        // create a new connection using a factory
        public Peer(String id, int endPoint) {
            Log.d(TAG, "new Peer: " + id + " " + endPoint);
            this.pc = factory.createPeerConnection(iceServers, pcConstraints, this);
            this.id = id;
            this.endPoint = endPoint;
            DataChannel localDataChannel;
            localDataChannel = this.pc.createDataChannel("sendDataChannel", new DataChannel.Init());

            localDataChannel.registerObserver(new DataChannel.Observer() {
                // if the state changes
                @Override
                public void onStateChange() {
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

                }
            });
            dataChannelArrayList.add(localDataChannel);
            Log.d(TAG, "DATA channel size is " + dataChannelArrayList.size());
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
    private Peer addPeer(String id, int endPoint) {
        Peer peer = new Peer(id, endPoint);
        peers.put(id, peer);

        endPoints[endPoint] = true;
        return peer;
    }

    private void removePeer(String id) {
        Peer peer = peers.get(id);
        peer.pc.close();
        peers.remove(peer.id);
        endPoints[peer.endPoint] = false;
    }
    // constructor
    public WebRtcClient(String host, Menu _context) {
        context = _context;
        PeerConnectionFactory.initializeAndroidGlobals(_context, true, true,false, null);
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
        client.connect();

        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("stun:23.21.150.121"));

        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"));
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        image = (ImageView) context.findViewById(R.id.receivedImage);

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
