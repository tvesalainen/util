# can0 -> multicast 224.4.4.4 4444
# use strace -X verbose -xx -e trace=socket,setsockopt,bind candump can0
# to find proper bind string for can port
CAN0=xf9760400000003000000506e248124e5d37e84e3d37e
CAN1=xf4760500000003000000505e6f8144058c7ea4038c7e
ANY= xf176000000000300000050fe00814415df7ea413df7e
CAN=$ANY
socat socket-datagram:29:3:1:$CAN,bind=$CAN,setsockopt-int=101:5:1 udp-datagram:224.4.4.4:4444
