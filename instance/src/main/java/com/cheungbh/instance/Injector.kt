package com.cheungbh.instance

import android.content.Context
import cheungbh.net.Net
import com.cheungbh.yogasdk.criteria.*
import com.cheungbh.yogasdk.domain.Pose
import com.cheungbh.yogasdk.net.INet
import com.cheungbh.yogasdk.usecases.*
import com.cheungbh.yogasdk.usecases.Navasana
import com.cheungbh.yogasdk.usecases.previous.Natarajasana

/** [Injector] will give the implementation of [YogaBase]
 * If you want a set the pose to Natarajasana, you can assign Injector(Pose.NATARAJASANA) to to this variable.
 * After setting the pose, you can use the score calculation and comment generation function w.r.t. this pose.
 * */
object Injector {

    private lateinit var netLibrary: Net
    private val processImage by lazy { ProcessImageYolo() }

//    private val adhoMukhaShivanasanaFeedback by lazy{ AdhoMukhaShivanasana() }
//    private val ardhaChandarasanaFeedback by lazy{ ArdhaChandarasana() }
//    private val ardhaUttanasanaFeecback by lazy{ ArdhaUttanasana() }
//    private val badhaKonasanaFeedback by lazy{ BaddhaKonasana() }
//    private val bujangasanaFeedback by lazy{ Bhujangasana() }
//    private val caturangaDandasanaFeedback by lazy{ CaturangaDandasana() }
//    private val dandasanaFeedback by lazy { Dandasana() }
//    private val halasanaFeedback by lazy { Halasana() }
    private val natarajasanaFeedback by lazy{ Natarajasana() }
//    private val parivrttaPashvaKonasanaFeedback by lazy { ParivrttaPashvaKonasana() }
//    private val parivrttaTrikonasanaFeedback by lazy { ParivrttaTrikonasana() }
//    private val purnaShalabhasanaFeedback by lazy { PurnaShalabhasana() }
//    private val tuladandasanaFeecback by lazy{ Tuladandasana() }
//
//    private val ustrasanaFeedback by lazy{ Ustrasana() }
//    private val uttanaPadasanaFeedback by lazy { UttanaPadasana() }
//    private val ubhayaPadangushtasanaFeedback by lazy { UbhayaPadangushtasana() }
//    private val urdhvaDhanurasanaFeedback by lazy { UrdhvaDhanurasana() }
//    private val utthitaParsvakonasanaFeedback by lazy { UtthitaParsvakonasana() }
//    private val utthitaHastaPadangusthasanaAFeedback by lazy { UtthitaHastaPadangusthasanaA() }
//    private val utthitaHastaPadangusthasanaBFeedback by lazy { UtthitaHastaPadangusthasanaA() }
//    private val utthitaHastaPadangusthasanaCFeedback by lazy { UtthitaHastaPadangusthasanaC() }
//    private val vrksasanaFeecback by lazy { Vrksasana() }


    // New actions in January
    private val balasanaFeedback by lazy { Balasana(angleBothLegs, angleBothWaists, angleBothHandKneeAnkle) }
    private val tpose by lazy{ Tpose(angleBothLegs, angleBothShoulders, angleBothArms)}
    private val navasanaFeedback by lazy{ Navasana(angleBothArms, angleBothLegs, angleBothWaists) }
    private val utthitaTrikonasanaRightFeedback by lazy { UtthitaTrikonasanaRight( angleBothArms, angleBothLegs, rightAngleWaist) }
    private val padangushthasanaFeedback by lazy { Padangushthasana(angleBothArms, angleBothLegs, angleBothShoulders, angleBothWaists) }
    private val utthitaPashvakonasanaBRightFeedback by lazy { UtthitaPashvakonasanaBRight(
        leftAngleArm, leftAngleLeg, angleBetweenLegs, rightDistShoulderKnee
    ) }
    private val purvattanasanaFeedback by lazy { Purvattanasana(angleBothShoulderHandAnkle, angleBothLegs, angleBothArms) }
    private val utthitaPashvakonasanaARightFeedback by lazy { UtthitaPashvakonasanaARight(
        angleBothArms, angleBothLegs, rightAngleWaist
    ) }
    private val ardhaUttanasanaFeedback by lazy { ArdhaUttanasana(angleBothArms, angleBothLegs, angleBothWaists) }
    private val utthitaPashvakonasanaALeftFeedback by lazy { UtthitaPashvakonasanaALeft(
        rightAngleArm, rightAngleLeg, angleBetweenLegs
    ) }
    private val utkatasanaFeedback by lazy { Utkatasana(angleBothArms, angleBothLegs, angleBothShoulders, angleBothWaists) }
    private val utthitaTrikonasanaLeftFeedback by lazy { UtthitaTrikonasanaLeft(angleBothArms, angleBothLegs, leftAngleWaist) }
    private val adhoMukhaShivanasanaFeedback by lazy { AdhoMukhaShivanasana(angleBothArms, angleBothLegs, angleBothWaists) }
    private val marjarasanaCFeedback by lazy { MarjarasanaC(angleBothLegs, angleBothShoulders, angleBothWaists) }
    private val marjarasanaBFeedback by lazy { MarjarasanaB(angleBothLegs, angleBothShoulders, angleBothWaists) }
    private val utthitaPashvakonasanaBLeftFeedback by lazy { UtthitaPashvakonasanaBLeft(
        rightAngleArm, rightAngleLeg, angleBetweenLegs, leftDistShoulderKnee
    ) }
    private val virabhadrasanaCLeftFeedback by lazy { VirabhadrasanaCLeft(leftAngleLeg, rightAngleLeg, leftAngleWaist) }
    private val virabhadrasanaCRightFeedback by lazy { VirabhadrasanaCRight(leftAngleLeg, rightAngleLeg, rightAngleWaist) }
    private val virabhadrasanaDLeftFeedback by lazy { VirabhadrasanaDLeft(leftAngleLeg, leftAngleWaist, angleBothArms) }
    private val virabhadrasanaDRightFeedback by lazy { VirabhadrasanaDRight(angleBothArms, rightAngleLeg, rightAngleWaist) }
    private val virabhadrasanaALeftFeedback by lazy { VirabhadrasanaALeft(leftAngleLeg, leftAngleWaist, angleBothArms) }
    private val virabhadrasanaARightFeedback by lazy { VirabhadrasanaARight(angleBothArms, rightAngleLeg, rightAngleWaist) }
    private val phalakasanaAFeedback by lazy { PhalakasanaA(angleBothArms, angleBothLegs, angleBothShoulderHandAnkle) }
    private val urdhvaMukhaSvanasanaFeedback by lazy { UrdhvaMukhaSvanasana(angleBothArms, angleBothShoulders, angleBothHandKneeAnkle) }
    private val ardhaPurvattanasanaFeedback by lazy { ArdhaPurvattanasana(angleBothArms, angleBothWaists, angleBothLegs) }
    private val bujangasanaFeedback by lazy { Bujangasana(angleBothArms, angleBothShoulders, angleBothHandKneeAnkle) }
    private val phalakasanaBFeedback by lazy { PhalakasanaB(angleBothShoulders, angleBothLegs, angleBothShoulderHandAnkle)}
    private val setuBandhasanaFeedback by lazy { SetuBandhasana(angleBothShoulderHandAnkle, angleBothLegs, distBothHandAnkle) }
    private val urdhvaDhanurasanaFeedback by lazy { UrdhvaDhanurasana(angleBothArms, angleBothShoulderHandAnkle, distBothHandAnkle) }
    private val virabhadrasanaBLeftFeedback by lazy { VirabhadrasanaBLeft(leftAngleLeg, angleBothShoulders, leftAngleWaist)}
    private val virabhadrasanaBRightFeedback by lazy { VirabhadrasanaBRight(rightAngleLeg, angleBothShoulders, rightAngleWaist)}
    private val ardhaVasisthasanaFeedback by lazy { ArdhaVasisthasana(angleBothArms, angleBothLegs, angleBothWaists) }

    // Criterion
    private val leftAngleArm by lazy { AngleSingleArm() }
    private val rightAngleArm by lazy { AngleSingleArm() }
    private val leftAngleLeg by lazy { AngleSingleLeg() }
    private val rightAngleLeg by lazy { AngleSingleLeg() }
    private val leftAngleShoulder by lazy { AngleSingleShoulder() }
    private val rightAngleShoulder by lazy { AngleSingleShoulder() }
    private val leftAngleWaist by lazy { AngleSingleWaist() }
    private val rightAngleWaist by lazy { AngleSingleWaist() }
    private val leftAngleHandKneeAnkle by lazy { AngleSingleHandKneeAnkle() }
    private val rightAngleHandKneeAnkle by lazy { AngleSingleHandKneeAnkle() }
    private val leftAngleShoulderHandAnkle by lazy { AngleSingleShoulderHandAnkle() }
    private val rightAngleShoulderHandAnkle by lazy { AngleSingleShoulderHandAnkle() }

    private val angleBothLegs by lazy { com.cheungbh.yogasdk.criteria.AngleBothLegs() }
    private val angleBothShoulders by lazy { com.cheungbh.yogasdk.criteria.AngleBothShoulders() }
    private val angleBothArms by lazy { com.cheungbh.yogasdk.criteria.AngleBothArms() }
    private val angleBothWaists by lazy { com.cheungbh.yogasdk.criteria.AngleBothWaists() }
    private val angleBetweenLegs by lazy { com.cheungbh.yogasdk.criteria.AngleBetweenLegs() }
    private val angleBothShoulderHandAnkle by lazy { com.cheungbh.yogasdk.criteria.AngleBothShoulderHandAnkle() }
    private val angleBothHandKneeAnkle by lazy { com.cheungbh.yogasdk.criteria.AngleBothHandKneeAnkle() }
    private val angleBothHandElbowAnkle by lazy { com.cheungbh.yogasdk.criteria.AngleBothHandElbowAnkle() }

    private val distBothHandAnkle by lazy { DistBothHandAnkle() }

    private val leftDistHandAnkle by lazy { DistSingleHandAnkle() }
    private val rightDistHandAnkle by lazy { DistSingleHandAnkle() }
    private val leftDistShoulderKnee by lazy { DistSingleShoulderKnee() }
    private val rightDistShoulderKnee by lazy { DistSingleShoulderKnee() }
    private val leftDistElbowKnee by lazy { DistSingleElbowKnee() }
    private val rightDistElbowKnee by lazy { DistSingleElbowKnee() }

    fun selectPose(poseName: String): YogaBase {
        return when(poseName){
//            Pose.CaturangaDandasana -> caturangaDandasanaFeedback
//            Pose.Natarajasana -> natarajasanaFeedback
//            Pose.Navasana -> navasanaFeedback
//            Pose.Ustrasana -> ustrasanaFeedback
//            Pose.BaddhaKonasana -> badhaKonasanaFeedback
//            Pose.Bhujangasana -> bujangasanaFeedback
//            Pose.AdhoMukhaShivanasana -> adhoMukhaShivanasanaFeedback
//            Pose.ArdhaChandarasana -> ardhaChandarasanaFeedback
//            Pose.Tuladandasana -> tuladandasanaFeecback
//            Pose.TPose -> tpose
//            Pose.TPoseNew2 -> tposenew
//            Pose.UtthitaParsvakonasana -> utthitaParsvakonasanaFeedback
//            Pose.UtthitaHastaPadangusthasanaA -> utthitaHastaPadangusthasanaAFeedback
//            Pose.UtthitaHastaPadangusthasanaB -> utthitaHastaPadangusthasanaBFeedback
//            Pose.UtthitaHastaPadangusthasanaC -> utthitaHastaPadangusthasanaCFeedback
//            Pose.Vrksasana -> vrksasanaFeecback
//            Pose.Dandasana -> dandasanaFeedback
//            Pose.ParivrttaPashvaKonasana -> parivrttaPashvaKonasanaFeedback
//            Pose.ParivrttaTrikonasana -> parivrttaTrikonasanaFeedback
//            Pose.PurnaShalabhasana -> purnaShalabhasanaFeedback
//            Pose.UbhayaPadangushtasana -> ubhayaPadangushtasanaFeedback
//            Pose.UrdhvaDhanurasana -> urdhvaDhanurasanaFeedback
//            Pose.UttanaPadasana -> uttanaPadasanaFeedback
//            Pose.Halasana -> halasanaFeedback

            // New actions in January

            Pose.Balasana -> balasanaFeedback
            Pose.Navasana -> navasanaFeedback
            Pose.UtthitaTrikonasanaRight -> utthitaTrikonasanaRightFeedback
            Pose.padangushthasana -> padangushthasanaFeedback
            Pose.UtthitaPashvakonasanaBRight -> utthitaPashvakonasanaBRightFeedback
            Pose.Purvattanasana -> purvattanasanaFeedback
            Pose.UtthitaPashvakonasanaARight -> utthitaPashvakonasanaARightFeedback
            Pose.ArdhaUttanasana -> ardhaUttanasanaFeedback
            Pose.UtthitaPashvakonasanaALeft -> utthitaPashvakonasanaALeftFeedback
            Pose.Utkatasana -> utkatasanaFeedback
            Pose.UtthitaTrikonasanaLeft -> utthitaTrikonasanaLeftFeedback
            Pose.AdhoMukhaShivanasana -> adhoMukhaShivanasanaFeedback
            Pose.MarjarasanaC -> marjarasanaCFeedback
            Pose.MarjarasanaB -> marjarasanaBFeedback
            Pose.UtthitaPashvakonasanaBLeft -> utthitaPashvakonasanaBLeftFeedback
            Pose.VirabhadrasanaCLeft -> virabhadrasanaCLeftFeedback
            Pose.VirabhadrasanaCRight -> virabhadrasanaCRightFeedback
            Pose.VirabhadrasanaDLeft -> virabhadrasanaDLeftFeedback
            Pose.VirabhadrasanaDRight -> virabhadrasanaDRightFeedback
            Pose.VirabhadrasanaALeft -> virabhadrasanaALeftFeedback
            Pose.VirabhadrasanaARight -> virabhadrasanaARightFeedback
            Pose.PhalakasanaA -> phalakasanaAFeedback
            Pose.UrdhvaMukhaSvanasana -> urdhvaMukhaSvanasanaFeedback
            Pose.ArdhaPurvattanasana -> ardhaPurvattanasanaFeedback
            Pose.Bujangasana -> bujangasanaFeedback
            Pose.PhalakasanaB -> phalakasanaBFeedback
            Pose.SetuBandhasana -> setuBandhasanaFeedback
            Pose.UrdhvaDhanurasana -> urdhvaDhanurasanaFeedback
            Pose.VirabhadrasanaBLeft -> virabhadrasanaBLeftFeedback
            Pose.VirabhadrasanaBRight -> virabhadrasanaBRightFeedback
            Pose.ArdhaVasisthasana -> ardhaVasisthasanaFeedback
//            else -> WrongPosture
            else -> natarajasanaFeedback
        }
    }

    fun setNetLibrary(context: Context){

        netLibrary = Net(context)
    }
    fun closeLibrary(){
        netLibrary.close()
    }
    fun getNetLibrary(): INet = netLibrary
    fun getProcessImg(): ProcessImageYolo = processImage
}