package org.firstinspires.ftc.teamcode;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * TeleOp base da TainhaTec - temporada 2026-2027.
 *
 * Regra de ouro: TODO numero ajustavel mora em Tunagens.java.
 * Aqui e so a logica - recalibrar nunca deve exigir mexer neste arquivo.
 *
 * Mapeamento dos controles:
 *
 * GAMEPAD 1 - piloto (dirigir):
 *   Stick esquerdo   -> andar (Y = frente/tras, X = lateral)
 *   Stick direito X  -> girar
 *   Gatilho esquerdo -> modo lento (segurar)
 *
 * GAMEPAD 2 - operador (mecanismos):
 *   Gatilho direito  -> intake puxando (analogico)
 *   Gatilho esquerdo -> intake pra tras (analogico)
 *   Bumper direito   -> liga/desliga o shooter (toggle)
 *   Bumper esquerdo  -> abre o portao do shooter (segurar = solta a bola)
 *   Botao B          -> shooter em reverso (segurar, desenroscar)
 *   Botao A          -> servo dos polens: coletar / largar (toggle)
 *
 * Obs.: mecanismo que nao existe no Driver Hub nao trava o TeleOp - ele so
 * avisa na telemetria e desativa aquele controle. Da pra testar a dirigicao
 * mesmo com o shooter ainda em redesenho.
 */
@TeleOp(name = "TeleOp")
public class TeleOp extends LinearOpMode {

    // ===== Hardware =====
    private Mecanum drivetrain;
    private DcMotorEx intake;
    private DcMotorEx shooter;
    private Servo shooterServo;
    private Servo polens;

    // ===== Estado dos toggles (deteccao de borda de subida) =====
    private boolean prevBumperDireito = false;
    private boolean prevBotaoA = false;
    private boolean shooterLigado = false;
    private boolean polensColetando = true;

    @Override
    public void runOpMode() {
        setupHardware();

        telemetry.addData("Status", "Pronto - aperte PLAY");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            driveControl();
            intakeControl();
            shooterControl();
            polensControl();
            updateTelemetry();
        }

        stopAll();
    }

    /**
     * Pega o hardware do robo. Drivetrain e obrigatorio (sem ele o TeleOp
     * nem comeca, com mensagem clara). Mecanismo ausente fica null e o
     * controle correspondente desativa sozinho.
     */
    private void setupHardware() {
        try {
            drivetrain = new Mecanum(hardwareMap, Tunagens.DRIVETRAIN);
        } catch (RuntimeException e) {
            throw new RuntimeException(
                    "Motor do drivetrain nao encontrado. Confere se os nomes em "
                    + "Tunagens.DRIVETRAIN batem com a configuracao do Robot Controller.", e);
        }

        intake = pegarMotor(Tunagens.INTAKE_NOME);
        shooter = pegarMotor(Tunagens.SHOOTER_NOME);
        shooterServo = pegarServo(Tunagens.SHOOTER_SERVO_NOME);
        polens = pegarServo(Tunagens.POLENS_NOME);

        if (shooterServo != null) shooterServo.setPosition(Tunagens.SHOOTER_SERVO_FECHADO);
        if (polens != null) polens.setPosition(Tunagens.POLENS_POSICAO_COLETAR);
    }

    /** Tenta pegar um motor; se nao estiver no config do Driver Hub, retorna null. */
    private DcMotorEx pegarMotor(String nome) {
        try {
            return hardwareMap.get(DcMotorEx.class, nome);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Tenta pegar um servo; se nao estiver no config do Driver Hub, retorna null. */
    private Servo pegarServo(String nome) {
        try {
            return hardwareMap.get(Servo.class, nome);
        } catch (RuntimeException e) {
            return null;
        }
    }

    // =====================================================================
    // GAMEPAD 1 - DIRIGIR
    // =====================================================================

    /**
     * Dirigicao mecanum centrada no robo, com o mesmo mapeamento de eixos do
     * "Driving Test" da PedroPathing (Tests.java). Se algum eixo andar
     * invertido, o ajuste certo e rodar o Mecanum Tuner e colar o resultado
     * em Tunagens.DRIVETRAIN - nao mexer aqui.
     */
    private void driveControl() {
        double forward = aplicarStick(-gamepad1.left_stick_y);
        double strafe = aplicarStick(-gamepad1.left_stick_x);
        double turn = aplicarStick(-gamepad1.right_stick_x);

        // Modo lento: gatilho esquerdo do gamepad 1
        double fator = (gamepad1.left_trigger > Tunagens.LIMIAR_GATILHO)
                ? Tunagens.FATOR_MODO_LENTO : 1.0;

        drivetrain.drive(new DrivePowers(forward * fator, strafe * fator, turn * fator), true);
    }

    /**
     * Aplica zona morta + curva de sensibilidade (quadratica) num eixo do
     * stick, reescalando o intervalo util. Devolve o valor conservando o sinal.
     */
    private double aplicarStick(double valor) {
        double magnitude = Math.abs(valor);
        if (magnitude < Tunagens.ZONA_MORTA_STICK) return 0.0;

        magnitude = (magnitude - Tunagens.ZONA_MORTA_STICK)
                / (1.0 - Tunagens.ZONA_MORTA_STICK);
        if (Tunagens.APLICAR_CURVA_STICK) magnitude *= magnitude;

        return Math.signum(valor) * magnitude;
    }

    // =====================================================================
    // GAMEPAD 2 - MECANISMOS
    // =====================================================================

    private void intakeControl() {
        if (intake == null) return;

        if (gamepad2.right_trigger > Tunagens.LIMIAR_GATILHO) {
            intake.setPower(gamepad2.right_trigger * Tunagens.INTAKE_POTENCIA);
        } else if (gamepad2.left_trigger > Tunagens.LIMIAR_GATILHO) {
            intake.setPower(-gamepad2.left_trigger * Tunagens.INTAKE_POTENCIA);
        } else {
            intake.setPower(0);
        }
    }

    private void shooterControl() {
        if (shooter != null) {
            // Toggle no bumper direito (borda de subida)
            boolean bumper = gamepad2.right_bumper;
            if (bumper && !prevBumperDireito) shooterLigado = !shooterLigado;
            prevBumperDireito = bumper;

            if (gamepad2.b) {
                shooter.setPower(Tunagens.SHOOTER_POTENCIA_REVERSA);
            } else if (shooterLigado) {
                shooter.setPower(Tunagens.SHOOTER_POTENCIA);
            } else {
                shooter.setPower(0);
            }
        }

        if (shooterServo != null) {
            // Segurar bumper esquerdo = portao aberto (solta a bola)
            double posicao = (gamepad2.left_bumper)
                    ? Tunagens.SHOOTER_SERVO_ABERTO
                    : Tunagens.SHOOTER_SERVO_FECHADO;
            shooterServo.setPosition(posicao);
        }
    }

    private void polensControl() {
        if (polens == null) return;

        // Toggle no botao A (borda de subida)
        boolean a = gamepad2.a;
        if (a && !prevBotaoA) polensColetando = !polensColetando;
        prevBotaoA = a;

        polens.setPosition(polensColetando
                ? Tunagens.POLENS_POSICAO_COLETAR
                : Tunagens.POLENS_POSICAO_LARGAR);
    }

    // =====================================================================
    // TELEMETRIA
    // =====================================================================

    private void updateTelemetry() {
        boolean modoLento = gamepad1.left_trigger > Tunagens.LIMIAR_GATILHO;
        telemetry.addData("Dirigicao", modoLento ? "MODO LENTO" : "NORMAL");

        // wheelPowers segue a ordem interna do PedroPathing: [FL, FR, BL, BR]
        telemetry.addData("Rodas FL/FR/BL/BR", "%.2f / %.2f / %.2f / %.2f",
                drivetrain.wheelPowers[0], drivetrain.wheelPowers[1],
                drivetrain.wheelPowers[2], drivetrain.wheelPowers[3]);

        if (shooter != null) {
            telemetry.addData("Shooter", shooterLigado ? "LIGADO" : "desligado");
        } else {
            telemetry.addData("Shooter", "NAO CONFIGURADO ('" + Tunagens.SHOOTER_NOME + "')");
        }

        if (intake != null) {
            telemetry.addData("Intake", "potencia %.2f", intake.getPower());
        } else {
            telemetry.addData("Intake", "NAO CONFIGURADO ('" + Tunagens.INTAKE_NOME + "')");
        }

        if (polens != null) {
            telemetry.addData("Polens", polensColetando ? "coletando" : "largando");
        } else {
            telemetry.addData("Polens", "NAO CONFIGURADO ('" + Tunagens.POLENS_NOME + "')");
        }

        telemetry.update();
    }

    /** Desliga tudo ao sair do TeleOp. */
    private void stopAll() {
        drivetrain.stop();
        if (intake != null) intake.setPower(0);
        if (shooter != null) shooter.setPower(0);
    }
}
