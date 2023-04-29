package com.uragiristereo.reversepixelify

import android.util.Log
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import de.robv.android.xposed.XposedBridge

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {
    companion object {
        const val TAG = "ReversePixelify"

        val attestationHooksClasses = listOf(
            AttestationHooksClass(
                rom = "PixelExperience",
                className = "com.android.internal.util.custom.AttestationHooks",
                methodName = "initApplicationBeforeOnCreate",
            ),
            AttestationHooksClass(
                rom = "EvolutionX",
                className = "com.android.internal.util.evolution.AttestationHooks",
                methodName = "initApplicationBeforeOnCreate",
            ),
            AttestationHooksClass(
                rom = "CrDroidAndroid",
                className = "com.android.internal.util.crdroid.AttestationHooks",
                methodName = "initApplicationBeforeOnCreate",
            ),
            AttestationHooksClass(
                rom = "Xtended",
                className = "com.android.internal.util.xtended.AttestationHooks",
                methodName = "initApplicationBeforeOnCreate",
            ),
        )

        val pixelPropsUtilClasses = listOf(
            PixelPropsUtilClass(
                rom = "PixelExperience",
                className = "com.android.internal.util.custom.PixelPropsUtils",
                methodName = "setProps",
            ),
            PixelPropsUtilClass(
                rom = "EvolutionX",
                className = "com.android.internal.util.evolution.PixelPropsUtils",
                methodName = "setProps",
            ),
            PixelPropsUtilClass(
                rom = "CrDroidAndroid",
                className = "com.android.internal.util.crdroid.PixelPropsUtils",
                methodName = "setProps",
            ),
            PixelPropsUtilClass(
                rom = "Xtended",
                className = "com.android.internal.util.xtended.PixelPropsUtils",
                methodName = "setProps",
            ),
        )
    }

    private val attestationHooksClass = getAttestationHooksClass()
    private val pixelPropUtilClass = getPixelPropsUtilClass()

    override fun onInit() = configs {
        isDebug = BuildConfig.DEBUG

        if (attestationHooksClass == null || pixelPropUtilClass == null) {
            log("Your ROM doesn't have internal spoofing or unsupported.")
        }
    }

    override fun onHook() = encase {
        if (attestationHooksClass != null) {
            loadApp {
                attestationHooksClass.className.hook {
                    injectMember {
                        method { name = attestationHooksClass.methodName }

                        beforeHook {
                            log("Revert spoofing for $packageName")

                            resultNull()
                        }
                    }
                }
            }
        }
        if (pixelPropUtilClass != null) {
            loadApp {
                pixelPropUtilClass.className.hook {
                    injectMember {
                        method { name = pixelPropUtilClass.methodName }

                        beforeHook {
                            log("Revert spoofing for $packageName")

                            resultNull()
                        }
                    }
                }
            }
        }
    }

    private fun getAttestationHooksClass(): AttestationHooksClass? {
        for (clazz in attestationHooksClass) {
            try {
                clazz.className.toClass()

                return clazz
            } catch (_: NoClassDefFoundError) { }
        }

        return null
    }

    private fun getPixelPropsUtilClass(): PixelPropsUtilClass? {
        for (clazz in pixelPropsUtilClasses) {
            try {
                clazz.className.toClass()

                return clazz
            } catch (_: NoClassDefFoundError) { }
        }

        return null
    }

    private fun log(message: String) {
        XposedBridge.log("[$TAG] $message")
        Log.i(TAG, message)
    }
}
