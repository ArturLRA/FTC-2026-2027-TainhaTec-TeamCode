package org.firstinspires.ftc.teamcode;

import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/**
 * ============================== TUNAGENS ==============================
 * Todo valor calibravel do robo fica neste arquivo. O TeleOp (e os futuros
 * autonomos) so leem daqui - recalibrar nunca deve exigir mexer na logica.
 *
 * Como preencher:
 *  1. DRIVETRAIN: rode o "Mecanum Tuner" na Driver Station e cole o codigo
 *     que ele gerar no bloco abaixo (nomes + direcoes dos motores).
 *  2. Mecanismos: os nomes tem que ser EXATAMENTE iguais aos da configuracao
 *     do Robot Controller (Driver Hub). Potencias e posicoes de servo saem
 *     de teste na bancada - muda aqui e recompila.
 *  3. Quando o localizer e o Foresight forem tunados (tuners da PedroPathing),
 *     os resultados tambem moram aqui, e o Constants.create() do pacote
 *     "pedro" referencia este mesmo DRIVETRAIN.
 */
public final class Tunagens {

    private Tunagens() {}

    // =====================================================================
    // DRIVETRAIN (saida do Mecanum Tuner da PedroPathing)
    //
    // Os nomes abaixo tem que bater com a configuracao do Robot Controller.
    // As direcoes sao o chute inicial padrao - rode o Mecanum Tuner e cole o
    // resultado gerado por cima disto.
    // =====================================================================
    public static final MecanumConfig DRIVETRAIN = new MecanumConfig(c -> {
        c.frontLeftName.set("frontLeft");
        c.frontRightName.set("frontRight");
        c.backLeftName.set("backLeft");
        c.backRightName.set("backRight");
        c.frontLeftDirection.set(DcMotorSimple.Direction.FORWARD);
        c.frontRightDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backLeftDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backRightDirection.set(DcMotorSimple.Direction.REVERSE);
        // true = motores travam (BRAKE) quando solta o stick
        // false = robo desliza ate parar (FLOAT)
        c.manualBrakeMode.set(true);
    });

    // =====================================================================
    // SENSIBILIDADE DA DIRIGICAO
    // =====================================================================

    /** Zona morta dos sticks (0 a 1) - menor que isso e ignorado. */
    public static final double ZONA_MORTA_STICK = 0.05;

    /** true = curva quadratica nos sticks (mais precisao perto do centro). */
    public static final boolean APLICAR_CURVA_STICK = true;

    /** Multiplicador de potencia do modo lento (gatilho esquerdo do G1). */
    public static final double FATOR_MODO_LENTO = 0.4;

    /** A partir daqui o gatilho conta como "pressionado" (0 a 1). */
    public static final double LIMIAR_GATILHO = 0.5;

    // =====================================================================
    // INTAKE
    // =====================================================================

    /** Nome do motor do intake no Robot Controller. */
    public static final String INTAKE_NOME = "intake";

    /** Potencia maxima do intake (0 a 1). */
    public static final double INTAKE_POTENCIA = 1.0;

    // =====================================================================
    // SHOOTER (novo design - bola sai por baixo)
    // =====================================================================

    /** Nome do motor principal do shooter no Robot Controller. */
    public static final String SHOOTER_NOME = "shooter";

    /** Potencia de arremesso (0 a 1). */
    public static final double SHOOTER_POTENCIA = 1.0;

    /** Potencia em reverso pra desenroscar a bola (valor negativo). */
    public static final double SHOOTER_POTENCIA_REVERSA = -0.4;

    /** Nome do servo do portao/gatilho do shooter. */
    public static final String SHOOTER_SERVO_NOME = "shooterServo";

    /** Posicao do servo com o portao fechado (bola retida). */
    public static final double SHOOTER_SERVO_FECHADO = 0.0;

    /** Posicao do servo com o portao aberto (bola solta). */
    public static final double SHOOTER_SERVO_ABERTO = 0.5;

    // =====================================================================
    // POLENS (mecanismo pra tirar os polens da flower)
    // =====================================================================

    /** Nome do servo do mecanismo dos polens. */
    public static final String POLENS_NOME = "polens";

    /** Posicao coletando (pegando o polen). */
    public static final double POLENS_POSICAO_COLETAR = 0.0;

    /** Posicao largando (soltando o polen). */
    public static final double POLENS_POSICAO_LARGAR = 0.5;
}
