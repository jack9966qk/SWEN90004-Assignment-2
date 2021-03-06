import java.io.FileNotFoundException;
import java.util.*;

/**
 * Main class for the simulation, runs from command line
 * Created by Jack on 3/5/2017.
 */
public class Simulation {
    private Arguments args;
    private Board board;
    private Csv csv;
    private int time = 0;

    /**
     * Run the simulation
     *
     * @param length time length of simulation
     */
    private void run(int length) {
        for (int i = 0; i < length; i++) {
            // activities of people
            Set<Person> people = new HashSet<>();
            people.addAll(board.getPeople());
            for (Person person : people) {
                Point p = person.getNextPos();
                harvest();
                person.moveEatAgeDie(p);
            }

            // activities of patches
            if (time % args.grainGrowthInterval == 0) {
                for (Patch patch : board.getPatches()) {
                    if (Constant.PROPORTIONAL_GROWTH_ENABLED) {
                        patch.addGrain((int) (patch.getMaxGrain() *
                                Constant.PATCH_GROWTH_PROPORTION));
                    } else {
                        patch.addGrain(args.grainGrowthRate);
                    }
                }
            }

            time += 1;

            // record data at this tick
            csv.record(board, time);
        }
    }

    /**
     * Perform harvest activity, take grain from patches equally distribute
     * to people at the patch
     */
    private void harvest() {
        double tax = 0;

        // get max wealth first
        int[] wealth = new int[args.numPeople];
        int i = 0;
        for (Person p : board.getPeople()) {
            wealth[i] = p.getGrain();
            i += 1;
        }
        Arrays.sort(wealth);
        int maxWealth = wealth[args.numPeople - 1];

        // harvest at each patch with people
        for (Map.Entry<Point, Set<Person>> entry :
                board.getAllPositions().entrySet()) {
            Point p = entry.getKey();
            Set<Person> people = entry.getValue();
            Patch patch = board.getPatchAt(p.getX(), p.getY());
            // NOTE integer division below
            double divided = patch.removeAll() / people.size();
            for (Person person : people) {
                if (Constant.TAXATION_ENABLED) {
                    // if rich, take a percentage of harvest
                    if (person.getWealthLevel(maxWealth).equals("high")) {
                        double taken = divided * Constant.TAX_PERCENTAGE;
                        person.addGrain((int) (divided - taken));
                        tax += taken;
                    }
                }
                // divide the grain equally to everyone
                person.addGrain((int) divided);
            }
        }

        if (Constant.TAXATION_ENABLED) {
            // if poor, give equally divided tax
            List<Person> poorPeople = new ArrayList<>();
            for (Person p : board.getPeople()) {
                if (p.getWealthLevel(maxWealth).equals("low")) {
                    poorPeople.add(p);
                }
            }
            double divided = tax / poorPeople.size();
            for (Person p : poorPeople) {
                p.addGrain((int) divided);
            }
        }

    }

    /**
     * Set up the simulation
     *
     * @throws FileNotFoundException CSV file cannot be created
     */
    private void setup(Arguments arguments) throws FileNotFoundException {
        args = arguments;
        board = new Board(args, Constant.BOARD_WIDTH, Constant.BOARD_HEIGHT);
        csv = new Csv(args);

        // put people to random positions
        Random random = new Random();
        for (int i = 0; i < args.numPeople; i++) {
            Person person = Person.makeRandom(args, random, board);
            int x, y;
            if (!Constant.SAME_POSITION_ENABLED) {
                x = random.nextInt(Constant.BOARD_WIDTH);
                y = random.nextInt(Constant.BOARD_HEIGHT);
            } else {
                x = Constant.BOARD_WIDTH / 2;
                y = Constant.BOARD_HEIGHT / 2;
            }
            board.put(person, x, y);
        }
        csv.record(board, time);
    }

    /**
     * The main function
     *
     * @param args command line arguments
     * @throws FileNotFoundException CSV file cannot be created
     */
    public static void main(String[] args) throws FileNotFoundException {
        // parse arguments
        Arguments arguments = new Arguments(args);

        // setup simulation
        Simulation simulation = new Simulation();
        simulation.setup(arguments);

        // run simulation
        simulation.run(arguments.timeMax);

        // save csv output
        simulation.csv.closeFile();
    }
}
