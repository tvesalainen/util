# can0 -> 
socat -d -d -u socket-datagram:29:3:1:xf9760400000003000000506e248124e5d37e84e3d37e,bind=xf9760400000003000000506e248124e5d37e84e3d37e,setsockopt-int=101:5:1 exec:"od -x"
