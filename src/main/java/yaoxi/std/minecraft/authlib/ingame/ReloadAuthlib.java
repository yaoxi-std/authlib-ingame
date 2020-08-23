package yaoxi.std.minecraft.authlib.ingame;

import com.sun.tools.attach.*;

import java.io.IOException;

public class ReloadAuthlib {
    public static void reload(String authNew) throws
            IOException, AttachNotSupportedException,
            AgentLoadException, AgentInitializationException {

        VirtualMachineDescriptor vmd = getVirtualMachineByName(
                "net.fabricmc.devlaunchinjector.Main"
        );
        VirtualMachine virtualMachine = null;
        if(vmd.id() == null){
            System.out.println("Java Virtual Machine not found.");
            return;
        }
        virtualMachine = VirtualMachine.attach(vmd.id());
            virtualMachine.loadAgent("authlib-injector.jar", authNew);
            virtualMachine.detach();
    }
    private static VirtualMachineDescriptor getVirtualMachineByName(String machineName){
        for(VirtualMachineDescriptor vmd : VirtualMachine.list()){
            System.out.println(vmd.displayName());
            if(vmd.displayName().equals(machineName)){
                return vmd;
            }
        }
        return null;
    }
}
