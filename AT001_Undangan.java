package com.mycompany.at001_undangan;

import java.util.Scanner;
import java.util.Random;
import java.util.HashMap;
import java.util.Stack;

public class AT001_Undangan {
    
    static Scanner scanner = new Scanner(System.in);
    static Random random = new Random();

    // Player stats
    static class Player {
        int maxHP = 1200;
        int hp = 1200;
        int atk = 150;
        int def = 80;
        int jinguStacks = 0;
        final int maxJinguStacks = 4;
        boolean jinguActive = false;
        int jinguCountervar = 0;
        Stack<Integer> jingustackduration = new Stack<>();

        boolean alive = true;

        // Calculate attack damage including Jingu Mastery
        int attackDamage() {
            if (!alive) return 0;
            int damage = atk;
            jinguCountervar += 1;
            jingustackduration.push(jinguCountervar);
            if (jinguStacks < maxJinguStacks) {
                jinguStacks++;
                System.out.println("Jingu Mastery stack gained! (" + jinguStacks + "/" + maxJinguStacks + ")");
            } else if (jinguStacks == maxJinguStacks) {
                jinguActive = true;
                System.out.println("Jingu Mastery Activated! Next hits deal bonus damage and lifesteal.");
            }
            if (jinguActive) {
                int bonus = 40;
                System.out.println("Jingu Mastery hit! Bonus damage +" + bonus);
                damage += bonus;
            }
            return damage;
        }

        // Calculate counter damage scaled on DEF with enhancement when hp <= 50%
        int counterDamage() {
            if (!alive) return 0;
            int baseDamage = def * 2;
            if (hp <= maxHP / 2) {
                baseDamage = (int)(baseDamage * 1.6);
            }
            // Include Jingu Mastery bonus if active
            if (jinguActive) {
                baseDamage += baseDamage * 40;
            }
            return baseDamage;
        }

        void heal(int amount) {
            if (!alive) return;
            hp = Math.min(hp + amount, maxHP);
            System.out.println("Player heals " + amount + " HP from Jingu Mastery lifesteal.");
        }

        void takeDamage(int damage) {
            if (!alive) return;
            hp -= damage;
            if (hp < 0) hp = 0;
            System.out.println("Player takes " + damage + " damage.");
            if (hp == 0) {
                alive = false;
                System.out.println("Player is defeated! Game Over.");
            }
        }
    }

    // Enemy stats
    static class Enemy {
        int maxHP = 1200;
        int hp = 1200;
        Stack<Integer> hpStack = new Stack<>();
        HashMap<String, Object> statuses = new HashMap<>(); // For poison stacks and timers
        boolean alive = true;
        String name; // Added to store monster's name

        void init() {
            hpStack.push(maxHP);
            statuses.put("poisonStacks", 0);
            statuses.put("poisonEndTime", 0L);
        }

        int getPoisonStacks() {
            return (int)statuses.get("poisonStacks");
        }

        void setPoisonStacks(int val) {
            statuses.put("poisonStacks", val);
        }

        long getPoisonEndTime() {
            return (long)statuses.get("poisonEndTime");
        }

        void setPoisonEndTime(long time) {
            statuses.put("poisonEndTime", time);
        }

        boolean isPoisonActive() {
            long now = System.currentTimeMillis();
            return getPoisonStacks() > 0 && now < getPoisonEndTime();
        }

        void takeDamage(int damage) {
            if (!alive) return;

            if (isPoisonActive()) {
                int newHp = hp + damage;
                if (newHp > maxHP) newHp = maxHP;
                boolean reverted = updateHpStack(newHp);
                if (!reverted) {
                    hp = newHp;
                    System.out.println("Enemy is poisoned and heals " + damage + " HP instead of taking damage.");
                }
            } else {
                int newHp = hp - damage;
                if (newHp < 0) newHp = 0;
                boolean reverted = updateHpStack(newHp);
                if (!reverted) {
                    hp = newHp;
                    System.out.println("Enemy takes " + damage + " damage.");
                }
                if (hp == 0) {
                    alive = false;
                    System.out.println("Enemy is defeated! You win!");
                }
            }
        }

        boolean updateHpStack(int newHp) {
            hpStack.push(newHp);
            if (hpStack.size() > 1) {
                double chance = random.nextDouble();
                if (chance < 0.25) {
                    hpStack.pop();
                    hp = hpStack.peek();
                    System.out.println("Enemy reverted to previous HP stack: " + hp);
                    return true;
                }
            }
            return false;
        }

        void applyPoison() {
            int stacks = getPoisonStacks();
            if (stacks < 3) {
                stacks++;
                setPoisonStacks(stacks);
                System.out.println("Enemy poisoned! Stacks: " + stacks);
            } else {
                System.out.println("Enemy poison stack maxed at 3.");
            }
            setPoisonEndTime(System.currentTimeMillis() + 13000L);
        }

        void attack(Player player) {
            if (!alive) return;
            int baseDamage = 140;
            int variance = random.nextInt(21) - 10; 
            int damage = baseDamage + variance;
            if (damage < 0) damage = 0;
            int damageToPlayer = damage - (player.def / 2);
            if (damageToPlayer < 0) damageToPlayer = 0;
            System.out.println("Enemy attacks and deals " + damageToPlayer + " damage to Player.");
            player.takeDamage(damageToPlayer);
        }
    }

    // Linked list Node to store monster names
    static class Node {
        String firstName;
        String lastName;
        Node next;

        Node(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.next = null;
        }
    }

    // Linked list to store and generate random monster names
    static class MonsterNameList {
        private Node head;
        private Random random;

        private String[] firstNames = {
            "Grog", "Zar", "Thorn", "Blitz", "Fang", "Rok", "Skull", "Vex", "Krull", "Zog",
            "Morg", "Justine", "Ninmar", "Mark", "Joshua",
        };

        private String[] lastNames = {
            "the Terrible", "the Fearsome", "the Mighty", "the Cunning", "the Ruthless",
            "Agustin", "Rosales", "Manuel", "Alcantara", "the Malevolent",
            "Ocampo", "Ampi", "Olpenado", "Palencia", "Reponoya"
        };

        public MonsterNameList() {
            this.head = null;
            this.random = new Random();
            generateRandomNames(25);
        }

        private void generateRandomNames(int count) {
            for (int i = 0; i < count; i++) {
                String firstName = firstNames[random.nextInt(firstNames.length)];
                String lastName = lastNames[random.nextInt(lastNames.length)];
                addName(firstName, lastName);
            }
        }

        private void addName(String firstName, String lastName) {
            Node newNode = new Node(firstName, lastName);
            if (head == null) {
                head = newNode;
            } else {
                Node current = head;
                while (current.next != null) {
                    current = current.next;
                }
                current.next = newNode;
            }
        }

        public String getRandomName() {
            int size = 0;
            Node current = head;
            while (current != null) {
                size++;
                current = current.next;
            }
            int index = random.nextInt(size);
            current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
            return current.firstName + " " + current.lastName;
        }

        // Optional: print all generated names
        public void printNames() {
            Node current = head;
            while (current != null) {
                System.out.println(current.firstName + " " + current.lastName);
                current = current.next;
            }
        }
    }

    public static void main(String[] args) {
        Player player = new Player();
        Enemy enemy = new Enemy();
        enemy.init();

        MonsterNameList monsterNames = new MonsterNameList();
        enemy.name = monsterNames.getRandomName();

        System.out.println("Turn-Based Game Started: Player VS Enemy \"" + enemy.name + "\"");
        System.out.println("Player HP: " + player.hp + "/" + player.maxHP + " | Enemy HP: " + enemy.hp + "/" + enemy.maxHP);

        boolean playerTurn = true;

        while (player.alive && enemy.alive) {
            if (playerTurn) {
                System.out.println("\nYour turn! Choose an action:");
                System.out.println("1. Attack");
                System.out.println("2. Use Skill (Counter)");
                System.out.print("Enter choice: ");
                String input = scanner.nextLine();

                if (input.equals("1")) {
                    int dmg = player.attackDamage();
                    enemy.takeDamage(dmg);

                    if (player.jinguActive) {
                        player.heal(12);
                        player.jinguStacks = 0;
                        player.jinguCountervar = 0;
                        player.jingustackduration.push(0);
                        player.jinguActive = false;
                    }
                } else if (input.equals("2")) {
                    int dmg = player.counterDamage();

                    if (random.nextDouble() < 0.3) {
                        enemy.applyPoison();
                    }
                    System.out.println("Player uses Counter skill dealing " + dmg + " damage!");
                    enemy.takeDamage(dmg);

                    player.jinguStacks = 0;
                    player.jinguActive = false;
                } else {
                    System.out.println("Invalid choice. Try again.");
                    continue;
                }

                System.out.printf("Player HP: %d/%d | Enemy HP: %d/%d\n", player.hp, player.maxHP, enemy.hp, enemy.maxHP);
                if (!enemy.alive) break;

                playerTurn = false;
            } else {
                System.out.println("\nEnemy's turn...");
                enemy.attack(player);

                System.out.printf("Player HP: %d/%d | Enemy HP: %d/%d\n", player.hp, player.maxHP, enemy.hp, enemy.maxHP);
                if (!player.alive) break;

                playerTurn = true;
            }
        }

        System.out.println("\nGame Over.");
        if (player.alive) {
            System.out.println("Congratulations! You won!");
        } else {
            System.out.println("You lost! Try again.");
        }
    }
}

