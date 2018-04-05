import java.io.File
import java.io.FileWriter
import java.util.*

/**
 * An interpreter for Quack programming language.
 *
 * Quack is a programming language presented at the 2004 Internet Problem Solving Contest (https://ipsc.ksp.sk/2004/real/problems/g.html).
 *
 * Quack Virtual Machine has a queue containing integer numbers ranging 0-65536. All operations are computed modulo 65536.
 * Additionally, the VM has 26 registers denoted by lowercase letters (a-z). Each register can hold one number.
 *
 * Quack program is represented by a sequence of commands separated by whitespaces or newline symbols.
 * There is a total of 18 commands, including mathematical operations, queue/register operations, output operations
 * and various operations implementing labeling commands and later jumping to them (incl. conditional jumping).
 *
 * @author  Viktor Kniazev
 * @since   2018-04-05
 *
 */

class QuackVM(inputFile: File, outputFile: File) {

    //Modulo const for queue
    private val QUEUE_MODULO: Int = 65536

    //Input/output utilities
    private val inputScanner = Scanner(inputFile)
    private val outputWriter = FileWriter(outputFile)

    //Main queue of the VM
    private val queue = Queue<Int>()
    //Array holding registers
    private val register = arrayOfNulls<Int>(26)

    //Unified list of compiled commands
    private val commandList = ArrayList<CommandAdapter>()
    //Map holding label association (pairs label - command index)
    private val labelMap = HashMap<String, Int>()

    //Current command index
    private var currentLine = 0
    //Termination flag
    private var terminated = false

    //Function to parse raw input program into a list of interpreted commands
    fun compile() {

        var currentCommand: String

        while (inputScanner.hasNext()) {

            currentCommand = inputScanner.next()

            //If current input is a number - put it into the queue
            if (isNumber(currentCommand)) {
                commandList.add(CommandAdapter(Command.INPUT, arrayOf(currentCommand)))
            }
            else {
                //Interpret the commands judging by the first symbol and parse args into raw string arrays along the way
                when (currentCommand[0]) {
                    '+' -> commandList.add(CommandAdapter(Command.PLUS, emptyArray()))
                    '-' -> commandList.add(CommandAdapter(Command.MINUS, emptyArray()))
                    '*' -> commandList.add(CommandAdapter(Command.MULT, emptyArray()))
                    '/' -> commandList.add(CommandAdapter(Command.DIV, emptyArray()))
                    '%' -> commandList.add(CommandAdapter(Command.MOD, emptyArray()))
                    '>' -> commandList.add(CommandAdapter(Command.REG_PUT, arrayOf(currentCommand[1].toString())))
                    '<' -> commandList.add(CommandAdapter(Command.REG_GET, arrayOf(currentCommand[1].toString())))
                    'P' -> {
                        if (currentCommand.length == 1)
                            commandList.add(CommandAdapter(Command.PRINT, emptyArray()))
                        else
                            commandList.add(CommandAdapter(Command.PRINT_REG, arrayOf(currentCommand[1].toString())))
                    }
                    'C' -> {
                        if (currentCommand.length == 1)
                            commandList.add(CommandAdapter(Command.PRINT_CHAR, emptyArray()))
                        else
                            commandList.add(CommandAdapter(Command.PRINT_REG_CHAR, arrayOf(currentCommand[1].toString())))
                    }
                    ':' -> {
                        //Mark commands while compiling to avoid runtime errors
                        labelMap.put(currentCommand.substring(1), currentLine)
                        commandList.add(CommandAdapter(Command.MARK, arrayOf(currentLine.toString(), currentCommand.substring(1))))
                    }
                    'J' -> commandList.add(CommandAdapter(Command.JUMP, arrayOf(currentCommand.substring(1))))
                    'Z' -> commandList.add(CommandAdapter(Command.JUMP_IF_0, arrayOf(currentCommand[1].toString(), currentCommand.substring(2))))
                    'E' -> commandList.add(CommandAdapter(Command.JUMP_IF_EQUAL, arrayOf(currentCommand.substring(1, 3), currentCommand.substring(3))))
                    'G' -> commandList.add(CommandAdapter(Command.JUMP_IF_MORE, arrayOf(currentCommand.substring(1, 3), currentCommand.substring(3))))
                    'Q' -> commandList.add(CommandAdapter(Command.QUIT, emptyArray()))
                }
            }

            //Do the carriage shift
            currentLine++

        }

        //Reset the carriage to prepare for running the program
        currentLine = 0

    }

    fun run() {

        //Execute commands one by one until the end of the program is reached or VM is terminated
        while (currentLine < commandList.size && !terminated) {
            commandList[currentLine].execute(this)
            currentLine++
        }

        outputWriter.close()

    }

    //Wrapper function to terminate the VM
    private fun terminate() { terminated = true }




    //Adapter class for command enum holding args and VM link
    private class CommandAdapter(val command: Command, private val args: Array<String>) {

        fun execute(vm: QuackVM) { command.execute(vm, args) }

    }

    /*
     *
     * Enum holding all command types.
     * Inside the enum the "execute" abstract method is declared, taking the VM link and a string array of raw args as input.
     * All enum members (command types) are represented by anonymous classes implementing the "execute" method
     * accordingly to their purpose.
     *
     * Most of the members' implementations are self-explanatory.
     *
     */
    private enum class Command {

        PLUS {
            override fun execute(vm: QuackVM, args: Array<String>) {
                if (vm.queue.isEmpty()) {
                    return
                }
                val a = vm.queue.remove()!!
                if (vm.queue.isEmpty()) {
                    return
                }
                val b = vm.queue.remove()!!
                vm.queue.add((a + b % vm.QUEUE_MODULO))
            }
        },
        MINUS {
            override fun execute(vm: QuackVM, args: Array<String>) {
                if (vm.queue.isEmpty()) {
                    return
                }
                val a = vm.queue.remove()!!
                if (vm.queue.isEmpty()) {
                    return
                }
                val b = vm.queue.remove()!!
                vm.queue.add((a - b % vm.QUEUE_MODULO))
            }
        },
        MULT {
            override fun execute(vm: QuackVM, args: Array<String>) {
                if (vm.queue.isEmpty()) {
                    return
                }
                val a = vm.queue.remove()!!
                if (vm.queue.isEmpty()) {
                    return
                }
                val b = vm.queue.remove()!!
                vm.queue.add((a * b % vm.QUEUE_MODULO))
            }
        },
        DIV {
            override fun execute(vm: QuackVM, args: Array<String>) {
                if (vm.queue.isEmpty()) {
                    return
                }
                val a = vm.queue.remove()!!
                if (vm.queue.isEmpty()) {
                    return
                }
                val b = vm.queue.remove()!!
                val result = try {
                    a / b
                } catch (e: ArithmeticException) {
                    0
                }
                vm.queue.add(result)
            }
        },
        MOD {
            override fun execute(vm: QuackVM, args: Array<String>) {
                if (vm.queue.isEmpty()) {
                    return
                }
                val a = vm.queue.remove()!!
                if (vm.queue.isEmpty()) {
                    return
                }
                val b = vm.queue.remove()!!
                val result = try {
                    a % b
                } catch (e: ArithmeticException) {
                    0
                }
                vm.queue.add(result)
            }
        },
        REG_PUT {
            override fun execute(vm: QuackVM, args: Array<String>) {
                val reg = args[0][0]
                if (vm.queue.isEmpty()) {
                    return
                }
                vm.register[reg.toInt() - 97] = vm.queue.remove()
            }
        },
        REG_GET {
            override fun execute(vm: QuackVM, args: Array<String>) {
                val reg = args[0][0]
                if (vm.register[reg.toInt() - 97] != null) vm.queue.add(vm.register[reg.toInt() - 97]!!)
            }
        },
        PRINT {
            override fun execute(vm: QuackVM, args: Array<String>) {
                if (!vm.queue.isEmpty()) {
                    vm.outputWriter.write(vm.queue.remove().toString())
                    vm.outputWriter.write("\n")
                }
            }
        },
        PRINT_REG {
            override fun execute(vm: QuackVM, args: Array<String>) {
                val reg = args[0][0]
                if (vm.register[reg.toInt() - 97] != null) {
                    vm.outputWriter.write(vm.register[reg.toInt() - 97].toString())
                    vm.outputWriter.write("\n")
                }
            }
        },
        PRINT_CHAR {
            override fun execute(vm: QuackVM, args: Array<String>) {
                if (!vm.queue.isEmpty()) vm.outputWriter.write((vm.queue.remove()!! % 256).toChar().toString())
            }
        },
        PRINT_REG_CHAR {
            override fun execute(vm: QuackVM, args: Array<String>) {
                val reg = args[0][0]
                if (vm.register[reg.toInt() - 97] != null) vm.outputWriter.write((vm.register[reg.toInt() - 97]!! % 256).toChar().toString())
            }
        },
        MARK {
            //In addition to compile-time labeling, also label in runtime to handle label override
            override fun execute(vm: QuackVM, args: Array<String>) {
                val line = Integer.parseInt(args[0])
                val label = args[1]
                vm.labelMap.put(label, line)
            }
        },
        JUMP {
            override fun execute(vm: QuackVM, args: Array<String>) {
                val label = args[0]
                if (vm.labelMap.containsKey(label)) vm.currentLine = vm.labelMap[label]!!
            }
        },
        JUMP_IF_0 {
            override fun execute(vm: QuackVM, args: Array<String>) {
                val reg = args[0][0]
                val label = args[1]
                if (vm.register[reg.toInt() - 97] != null && vm.register[reg.toInt() - 97]!! == 0)
                    if (vm.labelMap.containsKey(label)) vm.currentLine = vm.labelMap[label]!!
            }
        },
        JUMP_IF_EQUAL {
            override fun execute(vm: QuackVM, args: Array<String>) {
                val regA = args[0][0]
                val regB = args[0][1]
                val label = args[1]
                if (vm.register[regA.toInt() - 97] == vm.register[regB.toInt() - 97])
                    if (vm.labelMap.containsKey(label)) vm.currentLine = vm.labelMap[label]!!
            }
        },
        JUMP_IF_MORE {
            override fun execute(vm: QuackVM, args: Array<String>) {
                val regA = args[0][0]
                val regB = args[0][1]
                val label = args[1]
                if (vm.register[regA.toInt() - 97] != null && vm.register[regB.toInt() - 97] != null && vm.register[regA.toInt() - 97]!! > vm.register[regB.toInt() - 97]!!)
                    if (vm.labelMap.containsKey(label)) vm.currentLine = vm.labelMap[label]!!
            }
        },
        QUIT {
            override fun execute(vm: QuackVM, args: Array<String>) {
                vm.terminate()
            }
        },
        INPUT {
            override fun execute(vm: QuackVM, args: Array<String>) {
                vm.queue.add(args[0].toInt())
            }
        };

        abstract fun execute(vm: QuackVM, args: Array<String>)

    }

    //A simple method to check whether the current string is an int
    private fun isNumber(source: String): Boolean {

        try {
            Integer.parseInt(source)
        }
        catch (e: NumberFormatException) {
            return false
        }

        return true

    }

}