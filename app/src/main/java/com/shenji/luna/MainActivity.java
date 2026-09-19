package com.shenji.luna;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * 《神寂·巡礼》原型 —— 伊赛德亚 · 边界与谎言之庭
 * 布局：GL 视图（转视角） + HUD + 左下摇杆 + 右下跳跃键
 */
public class MainActivity extends Activity {

    private GameView gameView;
    private TextView hud;
    private TextView hint;          // ★ 纯白走廊里那行极淡的「我还在走」
    private View fade;              // ★ 坠落后的黑幕（黑两秒）
    private SplashScreen splash;
    private JoystickView joystick;
    private JumpButton jumpBtn;
    private TrialView trial;        // ★ 九酒之问 · 质询界面
    private NextRealmView next;     // ★ 猛地一闪 → 下一个神域
    private FrameLayout root;
    private TextView narrateView, crosshair, fireBtn;
    private final Runnable narrateHide = new Runnable() {
        @Override
        public void run() {
            if (narrateView != null) narrateView.animate().alpha(0f).setDuration(900L).start();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        root = new FrameLayout(this);

        gameView = new GameView(this);
        root.addView(gameView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // ★ 时力：跨副本永久能力（蒙尔斯洛斯通关奖励）
        if (getSharedPreferences("shili", MODE_PRIVATE).getBoolean("owned", false)) {
            gameView.setHasShili(true);
        }

        // ★ 蒙尔斯洛斯：旁白 / 准星 / 开火键
        narrateView = new TextView(this);
        narrateView.setTextColor(0xFFE8EDF8);
        narrateView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
        narrateView.setGravity(Gravity.CENTER);
        narrateView.setPadding(90, 0, 90, 0);
        narrateView.setAlpha(0f);
        FrameLayout.LayoutParams nlp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        nlp.gravity = Gravity.CENTER;
        root.addView(narrateView, nlp);

        crosshair = new TextView(this);
        crosshair.setText("┼");
        crosshair.setTextColor(0x99FFFFFF);
        crosshair.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        crosshair.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        clp.gravity = Gravity.CENTER;
        crosshair.setVisibility(View.GONE);
        root.addView(crosshair, clp);

        fireBtn = new TextView(this);
        fireBtn.setText("开火");
        fireBtn.setTextColor(0xFFF0D9A8);
        fireBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        fireBtn.setGravity(Gravity.CENTER);
        fireBtn.setBackgroundColor(0x44D9BE86);
        FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(150, 150);
        flp.gravity = Gravity.BOTTOM | Gravity.END;
        flp.setMargins(0, 0, 60, 220);
        fireBtn.setVisibility(View.GONE);
        root.addView(fireBtn, flp);
        fireBtn.setOnTouchListener(new View.OnTouchListener() {
            private final android.os.Handler h = new android.os.Handler();
            private final Runnable shiliRun = new Runnable() {
                @Override
                public void run() {
                    gameView.activateShili();
                }
            };
            @Override
            public boolean onTouch(View v, android.view.MotionEvent e) {
                switch (e.getActionMasked()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        h.postDelayed(shiliRun, 700L);
                        gameView.setFiring(true);
                        return true;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        h.removeCallbacks(shiliRun);
                        gameView.setFiring(false);
                        return true;
                }
                return true;
            }
        });

        hud = new TextView(this);
        hud.setTextColor(Color.WHITE);
        hud.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        hud.setPadding(46, 40, 46, 40);
        hud.setShadowLayer(5f, 0f, 0f, Color.BLACK);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        root.addView(hud, lp);

        // ★ 走廊提示：极淡、居中、只在「无界 · 纯白走廊」里出现。
        //   它的任务不是指引，而是声明「程序还活着」——避免玩家把白走廊当成白屏 bug。
        hint = new TextView(this);
        hint.setTextColor(0xFFB9B9BE);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        hint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        hint.setShadowLayer(4f, 0f, 0f, Color.BLACK);
        hint.setAlpha(0f);
        hint.setVisibility(View.GONE);
        FrameLayout.LayoutParams hlp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        hlp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        hlp.bottomMargin = dp(92);
        root.addView(hint, hlp);

        // 虚拟摇杆（左下）
        joystick = new JoystickView(this);
        FrameLayout.LayoutParams jlp = new FrameLayout.LayoutParams(dp(152), dp(152));
        jlp.gravity = Gravity.BOTTOM | Gravity.START;
        jlp.leftMargin = dp(34);
        jlp.bottomMargin = dp(24);
        root.addView(joystick, jlp);

        // 跳跃键（右下）
        jumpBtn = new JumpButton(this);
        FrameLayout.LayoutParams blp = new FrameLayout.LayoutParams(dp(112), dp(112));
        blp.gravity = Gravity.BOTTOM | Gravity.END;
        blp.rightMargin = dp(46);
        blp.bottomMargin = dp(30);
        root.addView(jumpBtn, blp);

        // ★ 坠落后的黑幕：先黑两秒，再睁眼 —— 睁眼时人正从走廊的地上爬起来
        fade = new View(this);
        fade.setBackgroundColor(Color.BLACK);
        fade.setAlpha(0f);
        fade.setClickable(true);
        fade.setVisibility(View.GONE);
        root.addView(fade, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // ★ 启动页（标题画面）—— 盖在最上层，轻触后淡出进入游戏
        splash = new SplashScreen(this);
        root.addView(splash, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // ★ 九酒之问 · 质询界面（覆盖层，默认隐藏）
        trial = new TrialView(this);
        trial.setVisibility(View.GONE);
        root.addView(trial, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // ★ 「猛地一闪」→ 第二位神域（覆盖层，默认隐藏）
        next = new NextRealmView(this);
        next.setVisibility(View.GONE);
        root.addView(next, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // 渲染加载进度 → 启动页进度条（着色器 / 纹理 / 音频 / 场景）
        gameView.setLoadListener(new GameRenderer.LoadListener() {
            @Override
            public void onLoadStep(final String label, final int pct) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (pct >= 100) splash.setReady();
                        else splash.setProgress(pct, label);
                    }
                });
            }
        });

        // ★月见：设置（灵敏度/视野/晃动/温和/声音），右上角小齿轮
        applySettings();
        TextView gear = new TextView(this);
        gear.setText("⚙");
        gear.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        gear.setTextColor(0x77FFFFFF);
        gear.setPadding(dp(18), dp(10), dp(24), dp(10));
        FrameLayout.LayoutParams glp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        glp.gravity = Gravity.TOP | Gravity.END;
        root.addView(gear, glp);
        gear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });

        setContentView(root);

        // ★ 隐藏系统 UI 必须在 setContentView 之后 —— 此刻 DecorView 已挂载到 window
        // 否则 PhoneWindow.getInsetsController() 会 NPE
        hideSystemUi();
        // API 30+ edge-to-edge 兜底：post 到 layout 完成后再做一次，规避个别 ROM 时序问题
        postEdgeToEdgeFix();

        joystick.setListener(new JoystickView.Listener() {
            @Override
            public void onMove(float x, float y) {
                gameView.setMoveVec(x, y);
            }
        });
        jumpBtn.setListener(new JumpButton.Listener() {
            @Override
            public void onJump() {
                gameView.jump();
            }
        });

        gameView.setHudListener(new GameView.HudListener() {
            @Override
            public void onHud(final String text) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hud.setText(text);
                    }
                });
            }
        });

        // ★ 觐见（走近祂 + 仰望双眼）→ 开始九酒之问
        gameView.setSummonListener(new GameRenderer.SummonListener() {
            @Override
            public void onSummoned() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startTrial();
                    }
                });
            }
        });

        // ★ 蒙尔斯洛斯：旁白 / 通关（授予时力）
        gameView.setEventListener(new GameRenderer.EventListener() {
            @Override
            public void onNarrate(final String text) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showNarrate(text);
                    }
                });
            }
            @Override
            public void onBossDefeated() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getSharedPreferences("shili", MODE_PRIVATE).edit()
                                .putBoolean("owned", true).apply();
                        crosshair.setVisibility(View.GONE);
                        fireBtn.setVisibility(View.GONE);
                        showNarrate("「海退了。你夺走了祂的一部分时间。」\n—— 获得「时力」：长按开火，二十秒归你");
                        root.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                gameView.enterTrialRoom();
                            }
                        }, 4200L);
                    }
                });
            }
        });

        // ★ 九酒之问 → 崩塌（答错）／九轮走完 → 坠入无界
        trial.setListener(new TrialView.Listener() {
            @Override
            public void onCollapse(final int abyss) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        enterAbyss(abyss);
                    }
                });
            }

            @Override
            public void onFinished(final int abyss) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        enterAbyss(abyss);
                    }
                });
            }
        });

        // ★ 第二位神域：轻触回到审判场（原型调试用）
        next.setListener(new NextRealmView.Listener() {
            @Override
            public void onTap() {
                recreate();
            }
        });

        // ★ 走到蒙尔斯洛斯脚下 → 世界海收尾（停下、仰望，再亮出「第二位神域」字卡）
        gameView.setSeaListener(new GameRenderer.SeaListener() {
            @Override
            public void onSeaArrived() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finishWhiteSea();
                    }
                });
            }
        });
    }

    /** ★ 幻境/战斗旁白：淡入 3.6s 淡出；授枪那一刻亮出准星与开火键 */
    private void showNarrate(String text) {
        if (narrateView == null) return;
        narrateView.removeCallbacks(narrateHide);
        narrateView.setText(text);
        narrateView.animate().cancel();
        narrateView.setAlpha(0f);
        narrateView.animate().alpha(1f).setDuration(500L).start();
        if (crosshair != null && crosshair.getVisibility() != View.VISIBLE && text.contains("枪")) {
            crosshair.setVisibility(View.VISIBLE);
            fireBtn.setVisibility(View.VISIBLE);
        }
        narrateView.postDelayed(narrateHide, 3600L);
    }

    private android.content.SharedPreferences prefs() {
        return getSharedPreferences("luna", MODE_PRIVATE);
    }

    private void applySettings() {
        android.content.SharedPreferences p = prefs();
        gameView.applyLunaSettings(
                p.getFloat("sens", 1f),
                p.getFloat("fov", 62f),
                p.getFloat("bob", 1f),
                p.getBoolean("gentle", false),
                p.getBoolean("sound", true));
    }

    private void openSettings() {
        final android.content.SharedPreferences p = prefs();
        android.widget.LinearLayout box = new android.widget.LinearLayout(this);
        box.setOrientation(android.widget.LinearLayout.VERTICAL);
        int m = dp(18);
        box.setPadding(m, m / 2, m, 0);

        final android.widget.TextView sensLbl = new android.widget.TextView(this);
        final android.widget.SeekBar sens = new android.widget.SeekBar(this);
        sens.setMax(120);
        sens.setProgress(Math.round((p.getFloat("sens", 1f) - 0.3f) / 1.2f * 120f));
        final android.widget.TextView fovLbl = new android.widget.TextView(this);
        final android.widget.SeekBar fov = new android.widget.SeekBar(this);
        fov.setMax(15);
        fov.setProgress(Math.round(p.getFloat("fov", 62f)) - 60);
        final android.widget.CheckBox gentle = new android.widget.CheckBox(this);
        gentle.setText("温和模式（崩塌放缓，先有黑屏预警）");
        gentle.setTextColor(0xFFFFFFFF);
        gentle.setChecked(p.getBoolean("gentle", false));
        final android.widget.CheckBox sound = new android.widget.CheckBox(this);
        sound.setText("音效");
        sound.setTextColor(0xFFFFFFFF);
        sound.setChecked(p.getBoolean("sound", true));
        final android.widget.CheckBox calm = new android.widget.CheckBox(this);
        calm.setText("减弱走路晃动（防晕）");
        calm.setTextColor(0xFFFFFFFF);
        calm.setChecked(p.getFloat("bob", 1f) < 0.9f);

        android.widget.SeekBar.OnSeekBarChangeListener lst =
                new android.widget.SeekBar.OnSeekBarChangeListener() {
                    @Override public void onProgressChanged(android.widget.SeekBar bar, int pr, boolean fromUser) {
                        if (bar == sens) sensLbl.setText(String.format("转身灵敏度  %d%%", 30 + pr));
                        if (bar == fov) fovLbl.setText(String.format("视野  %d°", 60 + pr));
                    }
                    @Override public void onStartTrackingTouch(android.widget.SeekBar bar) {}
                    @Override public void onStopTrackingTouch(android.widget.SeekBar bar) {}
                };
        sens.setOnSeekBarChangeListener(lst);
        fov.setOnSeekBarChangeListener(lst);
        sensLbl.setText(String.format("转身灵敏度  %d%%", 30 + sens.getProgress()));
        fovLbl.setText(String.format("视野  %d°", 60 + fov.getProgress()));
        sensLbl.setTextColor(0xFFCCCCCC);
        fovLbl.setTextColor(0xFFCCCCCC);

        box.addView(sensLbl); box.addView(sens);
        box.addView(fovLbl); box.addView(fov);
        box.addView(gentle); box.addView(sound); box.addView(calm);

        new android.app.AlertDialog.Builder(this)
                .setTitle("月见 · 设置")
                .setView(box)
                .setPositiveButton("好", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface d, int which) {
                        p.edit()
                                .putFloat("sens", 0.3f + 1.2f * sens.getProgress() / 120f)
                                .putFloat("fov", 60f + fov.getProgress())
                                .putFloat("bob", calm.isChecked() ? 0.4f : 1f)
                                .putBoolean("gentle", gentle.isChecked())
                                .putBoolean("sound", sound.isChecked())
                                .apply();
                        applySettings();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    // ================================================================
    //  ★ 九酒之问 → 坠入无界 → 无界·纯白走廊 → 猛地一闪 → 下一个神域
    // ================================================================

    /** 觐见：锁住操控，当即切进审判场镜室，再亮出质询界面 */
    private void startTrial() {
        gameView.freeze(true);
        gameView.enterTrialRoom();   // ★ 自动进入审判场 · 圆形镜室
        joystick.setVisibility(View.INVISIBLE);
        jumpBtn.setVisibility(View.INVISIBLE);
        hud.setVisibility(View.INVISIBLE);
        trial.reset();
        trial.setVisibility(View.VISIBLE);
    }

    /**
     * 坠入无界 —— 崩（答错）与九轮走完，终点是一样的：祂从不释放任何人，
     * 只是把你投递出去。唯一不同的是「死得多深」，而深度决定走廊走多久。
     *
     * 后半段的节奏：
     *   坠落 2.6s → 黑幕落下 0.5s → 全黑 2s → 睁眼（人已趴在走廊地上）→ 爬起来 2.0s
     *   → 站定顿一下 1.2s → 开始走（15~45s）→ 猛地一闪 → 下一个神域
     */
    private void enterAbyss(final int abyss) {
        trial.setVisibility(View.GONE);
        if (prefs().getBoolean("gentle", false)) {
            fade.setVisibility(View.VISIBLE);
            fade.animate().alpha(1f).setDuration(600L).start();
            root.postDelayed(new Runnable() {
                @Override
                public void run() {
                    gameView.collapse();
                    collapseSequence(abyss);
                }
            }, 1200L);
            return;
        }
        gameView.collapse();     // 铁律一：脚下「边界」具现被抹除
        collapseSequence(abyss);
    }

    private void collapseSequence(final int abyss) {
        root.postDelayed(new Runnable() {
            @Override
            public void run() {
                fade.setVisibility(View.VISIBLE);
                fade.animate().alpha(1f).setDuration(500L).start();
                root.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        gameView.enterCorridor();
                        long dwell = 13000L + abyss * 2000L + (long) (Math.random() * 16000.0);
                        if (dwell > 45000L) dwell = 45000L;
                        showCorridorHint(dwell);
                        fade.animate().alpha(0f).setDuration(900L).start();
                        root.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                fade.setVisibility(View.GONE);
                            }
                        }, 950L);
                        root.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                flashAndNext();
                            }
                        }, dwell);
                    }
                }, 2000L);
            }
        }, 2600L);
    }

    /** 猛的一闪：全屏纯白爆闪 + 一记短促高频耳鸣 */
    private void flashAndNext() {
        try {
            final android.media.ToneGenerator tg = new android.media.ToneGenerator(
                    android.media.AudioManager.STREAM_MUSIC, 90);
            tg.startTone(android.media.ToneGenerator.TONE_PROP_BEEP2, 260);
            root.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        tg.release();
                    } catch (Throwable ignored) {
                    }
                }
            }, 600L);
        } catch (Throwable ignored) {
        }
        hud.setVisibility(View.GONE);
        // ★ 一闪之后不是直达下一神域：坠入「世界海 · 白色之海」。
        //    这一次把方向盘交给玩家 —— 让他自己走向雾中的蒙尔斯洛斯。
        gameView.enterWhiteSea();
        // 海面上重新交还操控（走廊里曾把摇杆 / 跳跃收起）
        joystick.setVisibility(View.VISIBLE);
        jumpBtn.setVisibility(View.VISIBLE);
        // 极淡的引导（这一境属于听觉，提示从简）
        hint.setVisibility(View.VISIBLE);
        hint.setText("（海面无边，只有呼吸）");
        hint.setAlpha(0f);
        hint.animate().alpha(0.30f).setDuration(2000).start();
        root.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (hint.getVisibility() == View.VISIBLE) {
                    hint.setText("（向雾中的祂走去）");
                    hint.animate().alpha(0.30f).setDuration(1200).start();
                }
            }
        }, 9000L);
    }

    /**
     * ★ 走到蒙尔斯洛斯脚下 —— 世界海 · 收尾（A 方案）：
     *   收起操控 → 站定仰望 2.4 秒 → 亮出「第二位神域」字卡。
     */
    private void finishWhiteSea() {
        joystick.setVisibility(View.INVISIBLE);
        jumpBtn.setVisibility(View.INVISIBLE);
        hint.animate().alpha(0f).setDuration(900).start();
        root.postDelayed(new Runnable() {
            @Override
            public void run() {
                hint.setVisibility(View.GONE);
            }
        }, 950L);
        root.postDelayed(new Runnable() {
            @Override
            public void run() {
                gameView.freeze(true);   // 字卡期间锁住走动与转视角
                next.start();
            }
        }, 2400L);
    }

    /**
     * 纯白走廊里的三行「活着的证据」。
     * 白走廊原本的设计是「无 UI、无提示」，但实测会被当成卡死/白屏 bug；
     * 折中：保留极淡的三句话，且只在**后半程**出现，同时脚步声一直留着 —— 氛围不塌，疑心不涨。
     */
    private void showCorridorHint(final long dwell) {
        // 起身 + 顿一下 之前什么都不显示 —— 那会儿玩家正忙着「确认自己还活着」
        final long head = (long) ((GameRenderer.CORRIDOR_RISE + GameRenderer.CORRIDOR_PAUSE) * 1000f);

        hint.setVisibility(View.VISIBLE);
        hint.setText("（只有脚步声）");
        hint.setAlpha(0f);
        root.postDelayed(new Runnable() {
            @Override
            public void run() {
                hint.animate().alpha(0.52f).setDuration(1600).start();
            }
        }, head + 1200L);

        root.postDelayed(new Runnable() {
            @Override
            public void run() {
                hint.setText("（走了很久了）");
                hint.animate().alpha(0.42f).setDuration(1200).start();
            }
        }, Math.max(head + 3200L, dwell / 2));

        root.postDelayed(new Runnable() {
            @Override
            public void run() {
                hint.setText("（前面……好像有光）");
                hint.animate().alpha(0.62f).setDuration(1200).start();
            }
        }, Math.max(head + 4200L, dwell - 7000L));

        root.postDelayed(new Runnable() {
            @Override
            public void run() {
                gameView.corridorSilence();   // 最后几秒：连脚步声也抽走（呼应蒙尔斯洛斯的「听觉」）
            }
        }, Math.max(head + 2000L, dwell - 3500L));
    }

    private void hideSystemUi() {
        if (Build.VERSION.SDK_INT >= 30) {
            // Android 11+ (API 30+) 使用 WindowInsetsController
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
            // Android 16 (targetSdk 36) 要求 edge-to-edge，设置 DecorFitsSystemWindows
            getWindow().setDecorFitsSystemWindows(false);
        } else {
            // 旧版 Android 使用传统 API
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    // ★ API 30+ 的 setDecorFitsSystemWindows 在 onCreate 期间调用偶尔抛 NPE
    // 双保险：post 一个 Runnable 把 edge-to-edge 推到 layout 完成后执行
    private void postEdgeToEdgeFix() {
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        getWindow().setDecorFitsSystemWindows(false);
                        WindowInsetsController c = getWindow().getInsetsController();
                        if (c != null) {
                            c.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                            c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                        }
                    } catch (Exception ignored) {}
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        if (gameView != null) gameView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) gameView.onPause();
    }
}
