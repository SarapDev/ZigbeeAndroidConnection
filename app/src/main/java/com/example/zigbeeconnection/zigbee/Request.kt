package com.example.zigbeeconnection.zigbee

class Request (
     private var dstAddrMode: Int,
     private var dstAddress: Int = 0,
     private var dstEndpoint: Int = 0,
     private var profileID: Int = 0,
     private var clusterID: Int = 0,
     private var srcEndpoint: Int = 0,
     private var ASDULenght: Int = 0,
     private var ASDU: Int = 0,
     private var txOptions: Int = 0,
     private var useAliace: Int = 0,
     private var aliasSrcAddr: Int = 0,
     private var aliasSeqNumber: Int = 0,
     private var radiusCounter: Int = 0,
)  {
     fun toByteArray(): ByteArray {
          val res = ByteArray(1024)

          res[0] = dstAddrMode.toByte()
          res[1] = dstAddress.toByte()
          res[2] = dstEndpoint.toByte()
          res[3] = profileID.toByte()
          res[4] = clusterID.toByte()
          res[5] = srcEndpoint.toByte()
          res[6] = ASDULenght.toByte()
          res[7] = ASDU.toByte()
          res[8] = txOptions.toByte()
          res[9] = useAliace.toByte()
          res[10] = aliasSrcAddr.toByte()
          res[11] = aliasSeqNumber.toByte()
          res[12] = radiusCounter.toByte()

          return res
     }
}