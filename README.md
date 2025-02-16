# AmigaBlueKick

Amiga 500 Kickstart Selector with Bluetooth

It is common practice to store multiple Kickstart ROMs on large EPROMs that can accommodate more than 512 KB. In such cases, a switch is required to select the Kickstart to use. The selection is made using the higher address pins, setting them to 0 or 1. A 27C160 EPROM can hold four 512 KB Kickstart ROMs, meaning four selections are possible, requiring the last two address pins as selectors.

Traditionally, a mechanical switch is mounted outside the Amiga by drilling a hole in the plastic case (something I can't even imagine doing). More advanced methods use the reset wire to cycle through Kickstarts at each boot or based on how long the user holds Control-Amiga-Amiga, but even this method requires a wire soldered to the motherboard.

The idea is to make the switch wireless via Bluetooth. To achieve this, I used an ESP32S3 board (though an ESP32C3 should work just as well and costs less—I simply had an S3 spare at home). The board is wired to the Kickstart EPROM with power and two GPIOs connected to A18 and A19, allowing control via any Bluetooth (BLE) client, such as a computer or smartphone.

The code is very simple: it creates a Bluetooth service that accepts ASCII characters 0, 1, 2, and 3, and based on the input, it sets the GPIOs connected to the EPROM either HIGH or LOW.

The board is powered on only at the Amiga's first startup and remains active for 30 seconds. After that, it enters deep sleep, turning off everything except the GPIOs.

When the Kickstart is switched, the Amiga will become unresponsive because the CPU will not be properly initialized. A reboot is required.

So far, I have tested this only on an Amiga 500 rev 6A. I have no idea if it works on other machines, but I see no reason why it shouldn't.


<p align="center">
  <img src="https://github.com/emanuelelaface/AmigaBlueKick/blob/main/images/wiring.jpg" alt="Schematic" style="width: 50%;">
</p>
<div style="display: flex; justify-content: space-between;">
  <img src="https://github.com/emanuelelaface/AmigaBlueKick/blob/main/images/IMG_9770.JPG" alt="Image1" style="width: 32%;">
  <img src="https://github.com/emanuelelaface/AmigaBlueKick/blob/main/images/IMG_9771.JPG" alt="Image2" style="width: 32%;">
  <img src="https://github.com/emanuelelaface/AmigaBlueKick/blob/main/images/IMG_9772.JPG" alt="Image3" style="width: 32%;">
</div>


WARNING: DON'T CONNECT THE USB OF THE ESP32 WHEN IT IS CONNECTED TO THE AMIGA.
THE POWER WILL ARRIVE DIRECTLY TO THE KICKSTART AND THE MAIN BOARD AND MAY DESTROY THEM.

---

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
