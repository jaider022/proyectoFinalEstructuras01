const fs = require('fs/promises');
const { PDFParse } = require('pdf-parse');

async function main() {
    try {
        const dataBuffer = await fs.readFile('./Proyecto Final estructuras de datos dia 2026-1.pdf');
        const parser = new PDFParse({ data: dataBuffer });
        const data = await parser.getText();
        await parser.destroy();
        
        await fs.writeFile('./pdf_text.txt', data.text);
        console.log("Extraction complete.");
    } catch (error) {
        console.error(error);
    }
}

main();
