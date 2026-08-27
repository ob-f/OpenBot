package org.openbot.app.robot.server;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdServiceInfo;
import timber.log.Timber;

class NsdService {

  private static final String SERVICE_TYPE = "_openbot-server._tcp.";

  private final DiscoveryListener mDiscoveryListener =
      new DiscoveryListener() {

        @Override
        public void onDiscoveryStarted(String serviceType) {}

        @Override
        public void onServiceFound(NsdServiceInfo service) {
          // A service was found!  Do something with it.
          String name = service.getServiceName();
          String type = service.getServiceType();
          Timber.d("Service Name=%s, Type=%s", name, type);
          if (type.equals(SERVICE_TYPE)) {
            try {
              mNsdManager.resolveService(service, mResolveListener);
            } catch (IllegalArgumentException e) {
              Timber.w(e, "Unable to resolve openbot server service");
            }
          }
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
          // When the network service is no longer available.
          // Internal bookkeeping code goes here.
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {}

        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
          mNsdManager.stopServiceDiscovery(this);
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
          mNsdManager.stopServiceDiscovery(this);
        }
      };

  private ResolveListener mResolveListener;
  private NsdManager mNsdManager;

  public void start(Context context, ResolveListener resolveListener) {
    this.mResolveListener = resolveListener;
    Timber.d("Start discovery");
    mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
  }

  public void stop() {
    Timber.d("Stop discovery");
    mNsdManager.stopServiceDiscovery(mDiscoveryListener);
  }
}
