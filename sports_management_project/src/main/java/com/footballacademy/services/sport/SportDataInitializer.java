package com.footballacademy.services.sport;

import com.footballacademy.model.Sport;
import com.footballacademy.model.SportCategory;
import com.footballacademy.model.SportPosition;
import com.footballacademy.model.SportStatistic;
import com.footballacademy.repository.SportCategoryRepository;
import com.footballacademy.repository.SportRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;

@Component
public
class SportDataInitializer implements CommandLineRunner {
    private final SportRepository sportRepository;
    private final SportCategoryRepository sportCategoryRepository;
    private final SportService sportService;
    private final SportPositionService sportPositionService;
    private final SportStatisticService sportStatisticService;
    public SportDataInitializer(SportRepository sportRepository, SportCategoryRepository sportCategoryRepository, SportService sportService, SportPositionService sportPositionService, SportStatisticService sportStatisticService) {
        this.sportRepository = sportRepository;
        this.sportCategoryRepository = sportCategoryRepository;
        this.sportService = sportService;
        this.sportPositionService = sportPositionService;
        this.sportStatisticService = sportStatisticService;
    }
    @Override
    @Transactional
    public void run(String...args) {
        System.out.println("Sport catalog seed is disabled. Uncomment SportDataInitializer.run(...) seed block to run it once.");
        /*         // Manual one-time seed:         // 1. Uncomment this block.         // 2. Start the backend once.         // 3. Confirm the sports/categories in MySQL.         // 4. Comment this block again before the next normal startup.         if (sportRepository.count() == 0) {             initializeFootball();             initializeBasketball();             initializeHandball();             initializeSpeedball();         }         if (sportCategoryRepository.count() == 0) {             initializeCategories();         }         */
    }
    private void initializeCategories() {
        createCategory("U10", "U10", 1);
        createCategory("U13", "U13", 2);
        createCategory("U15", "U15", 3);
        createCategory("SENIOR", "Senior", 4);
        createCategory("ELITE", "Elite", 5);
        createCategory("BEGINNER", "Beginner", 6);
        createCategory("ADVANCED", "Advanced", 7);
    }
    private void createCategory(String code, String name, int order) {
        SportCategory category = new SportCategory(code, name);
        category.setDisplayOrder(order);
        category.setIsActive(true);
        sportCategoryRepository.save(category);
    }
    private void initializeFootball() {
        Sport football = new Sport("FOOTBALL", "Football");
        football.setDescription("Association football");
        football.setDisplayOrder(1);
        football = sportService.createSport(football);
        // Football positions
        SportPosition gk = new SportPosition("GK", "Goalkeeper");
        gk.setDescription("The player responsible for preventing the opposing team from scoring");
        gk.setDisplayOrder(1);
        gk.setSport(football);
        sportPositionService.createPosition(gk);
        SportPosition def = new SportPosition("DEF", "Defender");
        def.setDescription("Players who prevent the opposition from scoring");
        def.setDisplayOrder(2);
        def.setSport(football);
        sportPositionService.createPosition(def);
        SportPosition mid = new SportPosition("MID", "Midfielder");
        mid.setDescription("Players who play both attack and defense");
        mid.setDisplayOrder(3);
        mid.setSport(football);
        sportPositionService.createPosition(mid);
        SportPosition fwd = new SportPosition("FWD", "Forward");
        fwd.setDescription("Players who score goals");
        fwd.setDisplayOrder(4);
        fwd.setSport(football);
        sportPositionService.createPosition(fwd);
        // Football statistics
        SportStatistic goals = new SportStatistic("GOALS", "Goals", "INTEGER");
        goals.setDescription("Number of goals scored");
        goals.setIsRequired(true);
        goals.setDisplayOrder(1);
        goals.setSport(football);
        sportStatisticService.createStatistic(goals);
        SportStatistic assists = new SportStatistic("ASSISTS", "Assists", "INTEGER");
        assists.setDescription("Number of assists provided");
        assists.setIsRequired(true);
        assists.setDisplayOrder(2);
        assists.setSport(football);
        sportStatisticService.createStatistic(assists);
        SportStatistic tackles = new SportStatistic("TACKLES", "Tackles", "INTEGER");
        tackles.setDescription("Number of successful tackles");
        tackles.setIsRequired(false);
        tackles.setDisplayOrder(3);
        tackles.setSport(football);
        sportStatisticService.createStatistic(tackles);
        SportStatistic passes = new SportStatistic("PASSES", "Passes", "INTEGER");
        passes.setDescription("Number of successful passes");
        passes.setIsRequired(false);
        passes.setDisplayOrder(4);
        passes.setSport(football);
        sportStatisticService.createStatistic(passes);
        SportStatistic rating = new SportStatistic("RATING", "Average Rating", "DOUBLE");
        rating.setDescription("Average match rating (0-10)");
        rating.setIsRequired(true);
        rating.setDisplayOrder(5);
        rating.setSport(football);
        sportStatisticService.createStatistic(rating);
    }
    private void initializeBasketball() {
        Sport basketball = new Sport("BASKETBALL", "Basketball");
        basketball.setDescription("Basketball game");
        basketball.setDisplayOrder(2);
        basketball = sportService.createSport(basketball);
        // Basketball positions
        SportPosition pg = new SportPosition("PG", "Point Guard");
        pg.setDescription("Primary ball handler and playmaker");
        pg.setDisplayOrder(1);
        pg.setSport(basketball);
        sportPositionService.createPosition(pg);
        SportPosition sg = new SportPosition("SG", "Shooting Guard");
        sg.setDescription("Primary scorer and perimeter defender");
        sg.setDisplayOrder(2);
        sg.setSport(basketball);
        sportPositionService.createPosition(sg);
        SportPosition sf = new SportPosition("SF", "Small Forward");
        sf.setDescription("Versatile scorer and defender");
        sf.setDisplayOrder(3);
        sf.setSport(basketball);
        sportPositionService.createPosition(sf);
        SportPosition pf = new SportPosition("PF", "Power Forward");
        pf.setDescription("Inside scorer and rebounder");
        pf.setDisplayOrder(4);
        pf.setSport(basketball);
        sportPositionService.createPosition(pf);
        SportPosition c = new SportPosition("C", "Center");
        c.setDescription("Primary rebounder and inside defender");
        c.setDisplayOrder(5);
        c.setSport(basketball);
        sportPositionService.createPosition(c);
        // Basketball statistics
        SportStatistic points = new SportStatistic("POINTS", "Points", "INTEGER");
        points.setDescription("Total points scored");
        points.setIsRequired(true);
        points.setDisplayOrder(1);
        points.setSport(basketball);
        sportStatisticService.createStatistic(points);
        SportStatistic rebounds = new SportStatistic("REBOUNDS", "Rebounds", "INTEGER");
        rebounds.setDescription("Total rebounds");
        rebounds.setIsRequired(true);
        rebounds.setDisplayOrder(2);
        rebounds.setSport(basketball);
        sportStatisticService.createStatistic(rebounds);
        SportStatistic assists = new SportStatistic("ASSISTS", "Assists", "INTEGER");
        assists.setDescription("Total assists");
        assists.setIsRequired(true);
        assists.setDisplayOrder(3);
        assists.setSport(basketball);
        sportStatisticService.createStatistic(assists);
        SportStatistic steals = new SportStatistic("STEALS", "Steals", "INTEGER");
        steals.setDescription("Total steals");
        steals.setIsRequired(false);
        steals.setDisplayOrder(4);
        steals.setSport(basketball);
        sportStatisticService.createStatistic(steals);
        SportStatistic blocks = new SportStatistic("BLOCKS", "Blocks", "INTEGER");
        blocks.setDescription("Total blocks");
        blocks.setIsRequired(false);
        blocks.setDisplayOrder(5);
        blocks.setSport(basketball);
        sportStatisticService.createStatistic(blocks);
    }
    private void initializeHandball() {
        Sport handball = new Sport("HANDBALL", "Handball");
        handball.setDescription("Team handball");
        handball.setDisplayOrder(3);
        handball = sportService.createSport(handball);
        // Handball positions
        SportPosition gk = new SportPosition("GK", "Goalkeeper");
        gk.setDescription("Defends the goal");
        gk.setDisplayOrder(1);
        gk.setSport(handball);
        sportPositionService.createPosition(gk);
        SportPosition lw = new SportPosition("LW", "Left Wing");
        lw.setDescription("Left wing player");
        lw.setDisplayOrder(2);
        lw.setSport(handball);
        sportPositionService.createPosition(lw);
        SportPosition rw = new SportPosition("RW", "Right Wing");
        rw.setDescription("Right wing player");
        rw.setDisplayOrder(3);
        rw.setSport(handball);
        sportPositionService.createPosition(rw);
        SportPosition lb = new SportPosition("LB", "Left Back");
        lb.setDescription("Left back player");
        lb.setDisplayOrder(4);
        lb.setSport(handball);
        sportPositionService.createPosition(lb);
        SportPosition rb = new SportPosition("RB", "Right Back");
        rb.setDescription("Right back player");
        rb.setDisplayOrder(5);
        rb.setSport(handball);
        sportPositionService.createPosition(rb);
        SportPosition cb = new SportPosition("CB", "Center Back");
        cb.setDescription("Center back player");
        cb.setDisplayOrder(6);
        cb.setSport(handball);
        sportPositionService.createPosition(cb);
        SportPosition pv = new SportPosition("PV", "Pivot");
        pv.setDescription("Pivot player");
        pv.setDisplayOrder(7);
        pv.setSport(handball);
        sportPositionService.createPosition(pv);
        // Handball statistics
        SportStatistic goals = new SportStatistic("GOALS", "Goals", "INTEGER");
        goals.setDescription("Number of goals scored");
        goals.setIsRequired(true);
        goals.setDisplayOrder(1);
        goals.setSport(handball);
        sportStatisticService.createStatistic(goals);
        SportStatistic assists = new SportStatistic("ASSISTS", "Assists", "INTEGER");
        assists.setDescription("Number of assists");
        assists.setIsRequired(true);
        assists.setDisplayOrder(2);
        assists.setSport(handball);
        sportStatisticService.createStatistic(assists);
        SportStatistic saves = new SportStatistic("SAVES", "Saves", "INTEGER");
        saves.setDescription("Number of saves (goalkeeper)");
        saves.setIsRequired(false);
        saves.setDisplayOrder(3);
        saves.setSport(handball);
        sportStatisticService.createStatistic(saves);
    }
    private void initializeSpeedball() {
        Sport speedball = new Sport("SPEEDBALL", "Speedball");
        speedball.setDescription("Speedball game");
        speedball.setDisplayOrder(4);
        speedball = sportService.createSport(speedball);
        // Speedball positions (similar to football)
        SportPosition gk = new SportPosition("GK", "Goalkeeper");
        gk.setDescription("Goalkeeper");
        gk.setDisplayOrder(1);
        gk.setSport(speedball);
        sportPositionService.createPosition(gk);
        SportPosition def = new SportPosition("DEF", "Defender");
        def.setDescription("Defender");
        def.setDisplayOrder(2);
        def.setSport(speedball);
        sportPositionService.createPosition(def);
        SportPosition mid = new SportPosition("MID", "Midfielder");
        mid.setDescription("Midfielder");
        mid.setDisplayOrder(3);
        mid.setSport(speedball);
        sportPositionService.createPosition(mid);
        SportPosition fwd = new SportPosition("FWD", "Forward");
        fwd.setDescription("Forward");
        fwd.setDisplayOrder(4);
        fwd.setSport(speedball);
        sportPositionService.createPosition(fwd);
        // Speedball statistics
        SportStatistic goals = new SportStatistic("GOALS", "Goals", "INTEGER");
        goals.setDescription("Number of goals scored");
        goals.setIsRequired(true);
        goals.setDisplayOrder(1);
        goals.setSport(speedball);
        sportStatisticService.createStatistic(goals);
        SportStatistic assists = new SportStatistic("ASSISTS", "Assists", "INTEGER");
        assists.setDescription("Number of assists");
        assists.setIsRequired(true);
        assists.setDisplayOrder(2);
        assists.setSport(speedball);
        sportStatisticService.createStatistic(assists);
    }
}
