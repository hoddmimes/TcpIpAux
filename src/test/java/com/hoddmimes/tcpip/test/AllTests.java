package com.hoddmimes.tcpip.test;



        import org.junit.runner.RunWith;
        import org.junit.runners.Suite;
        import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        TestEncryption.class,
        TestNetwork.class })

public class AllTests {

}
