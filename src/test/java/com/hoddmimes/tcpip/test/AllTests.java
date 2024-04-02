package com.hoddmimes.tcpip.test;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({TestDH.class,
                TestEncryption.class,
                TestNetwork.class})


public class AllTests {

}
