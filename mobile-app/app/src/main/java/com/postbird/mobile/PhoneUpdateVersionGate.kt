package com.postbird.mobile

object PhoneUpdateVersionGate {
    fun shouldInstall(remoteCode: Int, currentCode: Int): Boolean {
        return remoteCode > currentCode
    }
}
