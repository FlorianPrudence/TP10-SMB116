package cnam.smb116.tp10;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;

public class MainActivity extends AppCompatActivity {

    private NsdManager.RegistrationListener registrationListener;
    private NsdManager.DiscoveryListener discoveryListener;
    private NsdManager.ResolveListener resolveListener;
    NsdManager mNsdManager;
    private String SERVICE_NAME = "Deptinfo";
    private String SERVICE_TYPE = "_http._tcp";
    private TextView tVServer, tVClient;
    private Button btnStartService, btnStopService, btnStartDiscover, btnStopDiscover, btnSendMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registrationListener = initializeRegistrationListener();
        resolveListener = initializeResolveListener();
        discoveryListener = initializeDiscoveryListener();
        btnStartService = findViewById(R.id.button_start_service);
        btnStopService = findViewById(R.id.button_stop_service);
        btnStartDiscover = findViewById(R.id.button_start_discovering);
        btnStopDiscover = findViewById(R.id.button_stop_discovering);
        btnSendMessage = findViewById(R.id.button_send_message);
        tVServer = findViewById(R.id.text_view_server);
        tVClient = findViewById(R.id.text_view_client);

        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    initializeServer();
                } catch (IOException e) {
                    tVServer.append("NSD Service Error:\n" + e + "\n");
                }
            }
        });

        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mNsdManager != null)
                    mNsdManager.unregisterService(registrationListener);
            }
        });

        btnStartDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mNsdManager != null)
                    mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
                tVServer.append("onServiceRegistered : " + SERVICE_NAME + "\n");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mNsdManager == null)
            mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
    }

    public void initializeServer() throws IOException {


        // Initialize a server socket on the next available port.
        ServerSocket serverSocket = new ServerSocket(0);
        // Get the chosen port.
        int port = serverSocket.getLocalPort();

        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);

        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
    }

    public NsdManager.RegistrationListener initializeRegistrationListener() {
        return new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                SERVICE_NAME = serviceInfo.getServiceName();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tVServer.append("onServiceRegistered : " + SERVICE_NAME + "\n");
                        btnStartService.setEnabled(false);
                        btnStopService.setEnabled(true);
                    }
                });

            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed! Put debugging code here to determine why.
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tVServer.append("onServiceUnregistered\n");
                        btnStartService.setEnabled(true);
                        btnStopService.setEnabled(false);
                    }
                });

            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed. Put debugging code here to determine why.
            }
        };
    }

    public NsdManager.DiscoveryListener initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        return new NsdManager.DiscoveryListener() {

            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tVClient.append("onServiceFound :" + service.getServiceType() + ", " + service.getServiceName() + "\n");
                    }
                });
                if (service.getServiceType().equals(SERVICE_TYPE) && service.getServiceName().equals(SERVICE_NAME)) {
                    mNsdManager.resolveService(service, resolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tVClient.append("onServiceLost\n");
                    }
                });
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tVClient.append("onDiscoveryStopped\n");
                    }
                });
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tVClient.append("onStartDiscoveryFailed\n");
                    }
                });
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tVClient.append("onStopDiscoveryFailed\n");
                    }
                });
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public NsdManager.ResolveListener initializeResolveListener() {
        return new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tVClient.append("onResolveFailed\n");
                    }
                });
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tVClient.append("onServiceResolved\n");
                    }
                });
            }
        };
    }
}