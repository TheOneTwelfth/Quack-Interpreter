import java.io.*

//Driver program
fun main(args: Array<String>) {

    val input = File("input.txt")
    val output = File("output.txt")

    val vm = QuackVM(input, output)
    vm.compile()
    vm.run()

}