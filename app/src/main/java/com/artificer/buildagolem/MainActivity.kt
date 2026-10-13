package com.artificer.buildagolem

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.artificer.buildagolem.audio.GameAudio
import com.artificer.buildagolem.game.BattleEngine
import com.artificer.buildagolem.game.BattleView
import com.artificer.buildagolem.model.*
import com.artificer.buildagolem.renderer.GolemPose
import com.artificer.buildagolem.storage.GameStateManager
import com.artificer.buildagolem.ui.CrucibleView
import com.artificer.buildagolem.ui.WorkshopView

class MainActivity : AppCompatActivity() {

    private lateinit var stateManager: GameStateManager

    // Navigation and Tab Views
    private lateinit var layoutWorkshop: View
    private lateinit var layoutCrucible: View
    private lateinit var layoutArena: View
    private lateinit var layoutCodex: View

    private lateinit var navWorkshop: Button
    private lateinit var navCrucible: Button
    private lateinit var navArena: Button
    private lateinit var navCodex: Button

    // Header Resources
    private lateinit var tvResourceGold: TextView
    private lateinit var tvResourceClay: TextView
    private lateinit var tvResourceOre: TextView
    private lateinit var tvResourceCrystal: TextView

    // Workshop UI
    private lateinit var workshopView: WorkshopView
    private lateinit var tvGolemName: TextView
    private lateinit var tvGolemStatsSummary: TextView
    private lateinit var tvGolemSynergies: TextView
    private lateinit var containerPartsList: LinearLayout
    private var selectedSlot: PartSlot = PartSlot.CORE

    // Crucible UI
    private lateinit var crucibleView: CrucibleView
    private lateinit var btnPumpBellows: Button
    private lateinit var btnStrikeAnvil: Button
    private lateinit var btnStartForging: Button

    // Arena & Combat UI
    private lateinit var containerStageSelect: View
    private lateinit var containerActiveBattle: View
    private lateinit var containerStagesList: LinearLayout
    private lateinit var battleView: BattleView
    private lateinit var tvEndlessHighScore: TextView

    // Combat Controls
    private lateinit var btnMoveLeft: Button
    private lateinit var btnMoveRight: Button
    private lateinit var btnCombatPunch: Button
    private lateinit var btnCombatCore: Button
    private lateinit var btnCombatShield: Button
    private lateinit var btnCombatOverdrive: Button
    private lateinit var btnExitCombat: Button

    private val combatHandler = Handler(Looper.getMainLooper())
    private var isMovingLeft = false
    private var isMovingRight = false

    private val movementRunnable = object : Runnable {
        override fun run() {
            val eng = battleView.engine
            if (eng != null && !eng.isVictory && !eng.isDefeat) {
                if (isMovingLeft) eng.movePlayer(-1f, 0.025f, battleView.width.toFloat())
                if (isMovingRight) eng.movePlayer(1f, 0.025f, battleView.width.toFloat())
            }
            if (isMovingLeft || isMovingRight) {
                combatHandler.postDelayed(this, 25)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        stateManager = GameStateManager(this)
        GameAudio.soundEnabled = stateManager.soundEnabled

        initViews()
        setupNavigation()
        setupWorkshop()
        setupCrucible()
        setupArena()
        setupSettings()

        updateHeaderResources()
        refreshWorkshopUI()
    }

    private fun initViews() {
        layoutWorkshop = findViewById(R.id.layoutWorkshop)
        layoutCrucible = findViewById(R.id.layoutCrucible)
        layoutArena = findViewById(R.id.layoutArena)
        layoutCodex = findViewById(R.id.layoutCodex)

        navWorkshop = findViewById(R.id.navWorkshop)
        navCrucible = findViewById(R.id.navCrucible)
        navArena = findViewById(R.id.navArena)
        navCodex = findViewById(R.id.navCodex)

        tvResourceGold = findViewById(R.id.tvResourceGold)
        tvResourceClay = findViewById(R.id.tvResourceClay)
        tvResourceOre = findViewById(R.id.tvResourceOre)
        tvResourceCrystal = findViewById(R.id.tvResourceCrystal)

        workshopView = findViewById(R.id.workshopView)
        tvGolemName = findViewById(R.id.tvGolemName)
        tvGolemStatsSummary = findViewById(R.id.tvGolemStatsSummary)
        tvGolemSynergies = findViewById(R.id.tvGolemSynergies)
        containerPartsList = findViewById(R.id.containerPartsList)

        crucibleView = findViewById(R.id.crucibleView)
        btnPumpBellows = findViewById(R.id.btnPumpBellows)
        btnStrikeAnvil = findViewById(R.id.btnStrikeAnvil)
        btnStartForging = findViewById(R.id.btnStartForging)

        containerStageSelect = findViewById(R.id.containerStageSelect)
        containerActiveBattle = findViewById(R.id.containerActiveBattle)
        containerStagesList = findViewById(R.id.containerStagesList)
        battleView = findViewById(R.id.battleView)
        tvEndlessHighScore = findViewById(R.id.tvEndlessHighScore)

        btnMoveLeft = findViewById(R.id.btnMoveLeft)
        btnMoveRight = findViewById(R.id.btnMoveRight)
        btnCombatPunch = findViewById(R.id.btnCombatPunch)
        btnCombatCore = findViewById(R.id.btnCombatCore)
        btnCombatShield = findViewById(R.id.btnCombatShield)
        btnCombatOverdrive = findViewById(R.id.btnCombatOverdrive)
        btnExitCombat = findViewById(R.id.btnExitCombat)
    }

    private fun updateHeaderResources() {
        val res = stateManager.resources
        tvResourceGold.text = "🪙 ${res.gold}"
        tvResourceClay.text = "🧱 ${res.clay}"
        tvResourceOre.text = "🔩 ${res.ironOre}"
        tvResourceCrystal.text = "🔮 ${res.crystals}"
    }

    private fun setupNavigation() {
        fun switchTab(view: View, selectedButton: Button) {
            GameAudio.playClick()
            layoutWorkshop.visibility = if (view == layoutWorkshop) View.VISIBLE else View.GONE
            layoutCrucible.visibility = if (view == layoutCrucible) View.VISIBLE else View.GONE
            layoutArena.visibility = if (view == layoutArena) View.VISIBLE else View.GONE
            layoutCodex.visibility = if (view == layoutCodex) View.VISIBLE else View.GONE

            val allButtons = listOf(navWorkshop, navCrucible, navArena, navCodex)
            for (btn in allButtons) {
                if (btn == selectedButton) {
                    btn.setTextColor(0xFFFFD166.toInt())
                } else {
                    btn.setTextColor(0xFFA09DB5.toInt())
                }
            }
        }

        navWorkshop.setOnClickListener { switchTab(layoutWorkshop, navWorkshop); refreshWorkshopUI() }
        navCrucible.setOnClickListener { switchTab(layoutCrucible, navCrucible) }
        navArena.setOnClickListener { switchTab(layoutArena, navArena); refreshArenaUI() }
        navCodex.setOnClickListener { switchTab(layoutCodex, navCodex) }
    }

    private fun setupWorkshop() {
        workshopView.currentConfig = stateManager.currentGolem

        workshopView.onSlotSelectedListener = { slot ->
            selectedSlot = slot
            highlightSlotButton(slot)
            populatePartsList()
        }

        findViewById<Button>(R.id.btnSlotCore).setOnClickListener { selectSlot(PartSlot.CORE) }
        findViewById<Button>(R.id.btnSlotHead).setOnClickListener { selectSlot(PartSlot.HEAD) }
        findViewById<Button>(R.id.btnSlotTorso).setOnClickListener { selectSlot(PartSlot.TORSO) }
        findViewById<Button>(R.id.btnSlotArms).setOnClickListener { selectSlot(PartSlot.ARMS) }
        findViewById<Button>(R.id.btnSlotLegs).setOnClickListener { selectSlot(PartSlot.LEGS) }

        findViewById<Button>(R.id.btnTestPunch).setOnClickListener {
            GameAudio.playPunchHit()
            workshopView.triggerPose(GolemPose.ATTACK_PUNCH, 700)
        }
        findViewById<Button>(R.id.btnTestBlock).setOnClickListener {
            GameAudio.playShieldBlock()
            workshopView.triggerPose(GolemPose.DEFEND_BLOCK, 900)
        }
        findViewById<Button>(R.id.btnTestOverdrive).setOnClickListener {
            GameAudio.playOverdrive()
            workshopView.triggerPose(GolemPose.OVERDRIVE_ROAR, 1400)
        }

        findViewById<Button>(R.id.btnRenameGolem).setOnClickListener {
            showRenameDialog()
        }
    }

    private fun selectSlot(slot: PartSlot) {
        GameAudio.playClick()
        selectedSlot = slot
        highlightSlotButton(slot)
        populatePartsList()
    }

    private fun highlightSlotButton(slot: PartSlot) {
        val slotButtons = mapOf(
            PartSlot.CORE to findViewById<Button>(R.id.btnSlotCore),
            PartSlot.HEAD to findViewById<Button>(R.id.btnSlotHead),
            PartSlot.TORSO to findViewById<Button>(R.id.btnSlotTorso),
            PartSlot.ARMS to findViewById<Button>(R.id.btnSlotArms),
            PartSlot.LEGS to findViewById<Button>(R.id.btnSlotLegs)
        )

        slotButtons.forEach { (s, btn) ->
            if (s == slot) {
                btn.setBackgroundResource(R.drawable.bg_gold_button)
                btn.setTextColor(0xFF12111A.toInt())
            } else {
                btn.setBackgroundResource(R.drawable.bg_action_button)
                btn.setTextColor(0xFFFFFFFF.toInt())
            }
        }
    }

    private fun refreshWorkshopUI() {
        val config = stateManager.currentGolem
        workshopView.currentConfig = config
        tvGolemName.text = config.name

        val stats = GolemDatabase.calculateStats(config)
        val speedStr = String.format("%.1f", stats.speed)
        tvGolemStatsSummary.text = "HP: ${stats.maxHp}  |  Armor: ${stats.armor}  |  Attack: ${stats.attack}  |  Speed: $speedStr"

        if (stats.activeSynergies.isNotEmpty()) {
            tvGolemSynergies.text = "⚡ Synergies:\n" + stats.activeSynergies.joinToString("\n")
            tvGolemSynergies.visibility = View.VISIBLE
        } else {
            tvGolemSynergies.text = "No Set Resonance yet (Equip 3+ matching element parts)"
            tvGolemSynergies.visibility = View.VISIBLE
        }

        populatePartsList()
    }

    private fun populatePartsList() {
        containerPartsList.removeAllViews()
        val parts = GolemDatabase.getPartsForSlot(selectedSlot)
        val currentEquippedId = when (selectedSlot) {
            PartSlot.CORE -> stateManager.currentGolem.coreId
            PartSlot.HEAD -> stateManager.currentGolem.headId
            PartSlot.TORSO -> stateManager.currentGolem.torsoId
            PartSlot.ARMS -> stateManager.currentGolem.armsId
            PartSlot.LEGS -> stateManager.currentGolem.legsId
        }

        for (part in parts) {
            val itemView = layoutInflater.inflate(R.layout.item_part, containerPartsList, false)
            val indicator = itemView.findViewById<View>(R.id.viewElementIndicator)
            val tvName = itemView.findViewById<TextView>(R.id.tvPartName)
            val tvRarity = itemView.findViewById<TextView>(R.id.tvPartRarity)
            val tvElement = itemView.findViewById<TextView>(R.id.tvPartElement)
            val tvStats = itemView.findViewById<TextView>(R.id.tvPartStats)
            val tvSpecial = itemView.findViewById<TextView>(R.id.tvPartSpecial)
            val btnAction = itemView.findViewById<Button>(R.id.btnPartAction)
            val tvCost = itemView.findViewById<TextView>(R.id.tvPartCost)

            indicator.setBackgroundColor(part.element.colorHex)
            tvName.text = part.name
            tvRarity.text = part.rarity.displayName
            tvRarity.setTextColor(part.rarity.colorHex)
            tvElement.text = "Element: ${part.element.displayName}"

            val speedText = if (part.speedBonus != 0f) {
                val sign = if (part.speedBonus > 0) "+" else ""
                " | ${sign}${String.format("%.1f", part.speedBonus)} Spd"
            } else ""
            tvStats.text = "+${part.hpBonus} HP | +${part.armorBonus} Def | +${part.attackBonus} Atk$speedText"
            tvSpecial.text = part.specialDescription

            val isUnlocked = stateManager.unlockedPartIds.contains(part.id)
            val isEquipped = (part.id == currentEquippedId)

            when {
                isEquipped -> {
                    btnAction.text = "EQUIPPED"
                    btnAction.setBackgroundResource(R.drawable.bg_action_button)
                    btnAction.setTextColor(0xFF06D6A0.toInt())
                    btnAction.isEnabled = false
                    tvCost.visibility = View.GONE
                }
                isUnlocked -> {
                    btnAction.text = "EQUIP"
                    btnAction.setBackgroundResource(R.drawable.bg_gold_button)
                    btnAction.setTextColor(0xFF12111A.toInt())
                    btnAction.isEnabled = true
                    tvCost.visibility = View.GONE
                    btnAction.setOnClickListener {
                        equipPart(part)
                    }
                }
                else -> {
                    btnAction.text = "CRAFT"
                    btnAction.setBackgroundResource(R.drawable.bg_action_button)
                    btnAction.setTextColor(0xFFFFD166.toInt())
                    tvCost.text = "${part.costGold}G | ${part.costClay}C | ${part.costOre}O | ${part.costCrystal}X"
                    tvCost.visibility = View.VISIBLE
                    btnAction.isEnabled = true
                    btnAction.setOnClickListener {
                        attemptCraftPart(part)
                    }
                }
            }

            containerPartsList.addView(itemView)
        }
    }

    private fun equipPart(part: GolemPart) {
        GameAudio.playEquip()
        val config = stateManager.currentGolem
        when (part.slot) {
            PartSlot.CORE -> config.coreId = part.id
            PartSlot.HEAD -> config.headId = part.id
            PartSlot.TORSO -> config.torsoId = part.id
            PartSlot.ARMS -> config.armsId = part.id
            PartSlot.LEGS -> config.legsId = part.id
        }
        stateManager.updateGolemConfig(config)
        refreshWorkshopUI()
    }

    private fun attemptCraftPart(part: GolemPart) {
        if (stateManager.canAfford(part)) {
            val success = stateManager.buyAndUnlockPart(part)
            if (success) {
                GameAudio.playHammerAnvil()
                Toast.makeText(this, "Forged ${part.name}!", Toast.LENGTH_SHORT).show()
                updateHeaderResources()
                equipPart(part)
            }
        } else {
            GameAudio.playDefeat()
            Toast.makeText(this, "Insufficient materials to forge this part!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRenameDialog() {
        val input = EditText(this)
        input.setText(stateManager.currentGolem.name)
        input.setSelectAllOnFocus(true)

        AlertDialog.Builder(this)
            .setTitle("Name Your Golem")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    stateManager.currentGolem.name = newName
                    stateManager.saveState()
                    refreshWorkshopUI()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupCrucible() {
        btnStartForging.setOnClickListener {
            val res = stateManager.resources
            if (res.clay >= 20 && res.ironOre >= 10) {
                res.clay -= 20
                res.ironOre -= 10
                stateManager.saveState()
                updateHeaderResources()
                crucibleView.startForgingSession()
            } else {
                Toast.makeText(this, "Need at least 20 Clay and 10 Iron Ore to forge!", Toast.LENGTH_SHORT).show()
            }
        }

        btnPumpBellows.setOnClickListener {
            crucibleView.pumpBellows()
        }

        btnStrikeAnvil.setOnClickListener {
            crucibleView.strikeAnvil()
        }

        crucibleView.onForgeCompletedListener = { _, qualityMultiplier ->
            val baseGold = 120
            val earnedGold = (baseGold * qualityMultiplier).toInt()
            val earnedCrystals = if (qualityMultiplier >= 1.5f) 4 else 1

            stateManager.addResources(gold = earnedGold, clay = 0, ore = 0, crystal = earnedCrystals)
            updateHeaderResources()
            GameAudio.playVictory()

            val rating = when {
                qualityMultiplier >= 2.0f -> "MYTHIC MASTERWORK"
                qualityMultiplier >= 1.5f -> "EPIC ARTISAN"
                else -> "STANDARD TEMPER"
            }

            AlertDialog.Builder(this)
                .setTitle("⚔ Forging Complete!")
                .setMessage("Quality Rating: $rating\n\nReward Earned:\n+$earnedGold Alchemical Gold\n+$earnedCrystals Arcane Crystals")
                .setPositiveButton("Claim", null)
                .show()
        }

        // Transmutations
        findViewById<Button>(R.id.btnTransmuteOre).setOnClickListener {
            if (stateManager.resources.clay >= 30) {
                stateManager.resources.clay -= 30
                stateManager.resources.ironOre += 15
                stateManager.saveState()
                updateHeaderResources()
                GameAudio.playCoreBlast()
                Toast.makeText(this, "Transmuted 30 Clay into 15 Iron Ore!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Need 30 Clay!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnTransmuteCrystal).setOnClickListener {
            if (stateManager.resources.ironOre >= 25) {
                stateManager.resources.ironOre -= 25
                stateManager.resources.crystals += 5
                stateManager.saveState()
                updateHeaderResources()
                GameAudio.playCoreBlast()
                Toast.makeText(this, "Transmuted 25 Iron Ore into 5 Arcane Crystals!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Need 25 Iron Ore!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupArena() {
        findViewById<Button>(R.id.btnDeployEndless).setOnClickListener {
            startCombat(stage = null, isEndless = true)
        }

        btnExitCombat.setOnClickListener {
            exitCombat()
        }

        setupCombatControls()
        refreshArenaUI()
    }

    private fun refreshArenaUI() {
        tvEndlessHighScore.text = "Personal High Score: ${stateManager.endlessHighScore}"
        containerStagesList.removeAllViews()

        for (stage in GolemDatabase.expeditionStages) {
            val stageView = layoutInflater.inflate(R.layout.item_stage, containerStagesList, false)
            val tvTitle = stageView.findViewById<TextView>(R.id.tvStageTitle)
            val tvDifficulty = stageView.findViewById<TextView>(R.id.tvStageDifficulty)
            val tvSubtitle = stageView.findViewById<TextView>(R.id.tvStageSubtitle)
            val tvRewards = stageView.findViewById<TextView>(R.id.tvStageRewards)
            val btnDeploy = stageView.findViewById<Button>(R.id.btnDeployStage)

            tvTitle.text = "Stage ${stage.stageNumber}: ${stage.title}"
            tvDifficulty.text = "Rec. Power: ${stage.recommendedPower}"
            tvSubtitle.text = stage.subtitle
            tvRewards.text = "Rewards: ${stage.goldReward}G | ${stage.clayReward} Clay | ${stage.oreReward} Ore | ${stage.crystalReward} Crystal"

            btnDeploy.setOnClickListener {
                startCombat(stage = stage, isEndless = false)
            }

            containerStagesList.addView(stageView)
        }
    }

    private fun startCombat(stage: ExpeditionStage?, isEndless: Boolean) {
        GameAudio.playEquip()
        containerStageSelect.visibility = View.GONE
        containerActiveBattle.visibility = View.VISIBLE

        val engine = BattleEngine(stateManager.currentGolem, stage, isEndless)
        battleView.engine = engine
    }

    private fun exitCombat() {
        val eng = battleView.engine
        if (eng != null) {
            // Award whatever resources were collected
            stateManager.addResources(
                gold = eng.collectedGold,
                clay = eng.collectedClay,
                ore = eng.collectedOre,
                crystal = eng.collectedCrystals
            )
            if (eng.isEndless) {
                stateManager.recordEndlessScore(
                    score = eng.score,
                    goldReward = 0,
                    clayReward = 0,
                    oreReward = 0,
                    crystalReward = 0
                )
            } else if (eng.isVictory && eng.stage != null) {
                stateManager.recordStageCompletion(
                    stageNumber = eng.stage.stageNumber,
                    gold = 0, clay = 0, ore = 0, crystal = 0 // already added above
                )
            }
            updateHeaderResources()
        }

        battleView.engine = null
        containerActiveBattle.visibility = View.GONE
        containerStageSelect.visibility = View.VISIBLE
        refreshArenaUI()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupCombatControls() {
        btnMoveLeft.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isMovingLeft = true
                    combatHandler.post(movementRunnable)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isMovingLeft = false
                    true
                }
                else -> false
            }
        }

        btnMoveRight.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isMovingRight = true
                    combatHandler.post(movementRunnable)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isMovingRight = false
                    true
                }
                else -> false
            }
        }

        btnCombatPunch.setOnClickListener {
            battleView.engine?.performPlayerPunch()
        }

        btnCombatCore.setOnClickListener {
            val eng = battleView.engine
            if (eng != null) {
                if (eng.coreSkillCooldown <= 0f) {
                    eng.performCoreSkill()
                } else {
                    Toast.makeText(this, "Core Skill recharging...", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnCombatShield.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    battleView.engine?.setPlayerBlocking(true)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    battleView.engine?.setPlayerBlocking(false)
                    true
                }
                else -> false
            }
        }

        btnCombatOverdrive.setOnClickListener {
            val eng = battleView.engine
            if (eng != null) {
                if (eng.overdriveEnergy >= 99f) {
                    eng.performOverdrive()
                } else {
                    Toast.makeText(this, "Charge Overdrive meter with punch combos!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupSettings() {
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            val soundTitle = if (stateManager.soundEnabled) "Disable Sound FX" else "Enable Sound FX"
            val options = arrayOf(soundTitle, "Reset Game Progress", "About Build a Golem")

            AlertDialog.Builder(this)
                .setTitle("⚙ Artificer Settings")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            stateManager.soundEnabled = !stateManager.soundEnabled
                            GameAudio.soundEnabled = stateManager.soundEnabled
                            stateManager.saveState()
                            Toast.makeText(this, "Sound: ${if (stateManager.soundEnabled) "ON" else "OFF"}", Toast.LENGTH_SHORT).show()
                        }
                        1 -> {
                            AlertDialog.Builder(this)
                                .setTitle("Reset Progress?")
                                .setMessage("This will reset all unlocked parts, resources, and stages. Are you sure?")
                                .setPositiveButton("Reset") { _, _ ->
                                    stateManager.resetProgress()
                                    updateHeaderResources()
                                    refreshWorkshopUI()
                                    Toast.makeText(this, "Progress reset.", Toast.LENGTH_SHORT).show()
                                }
                                .setNegativeButton("Cancel", null)
                                .show()
                        }
                        2 -> {
                            AlertDialog.Builder(this)
                                .setTitle("About Build a Golem")
                                .setMessage("Build a Golem v1.0\nCreated with Kotlin & Hardware-Accelerated 2D Canvas.\nAssemble parts, forge elements, and defend Eldermount Sanctum!")
                                .setPositiveButton("OK", null)
                                .show()
                        }
                    }
                }
                .show()
        }
    }
}
