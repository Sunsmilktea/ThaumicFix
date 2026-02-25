#!/usr/bin/env python3
"""
Deobfuscation script for Thaumicmixins
Replaces obfuscated names in source code with readable names from CSV files
"""

import os
import re
import csv

def load_mappings():
    """Load obfuscated name mappings from CSV files"""
    mappings = {
        'fields': {},  # searge -> name
        'methods': {}  # searge -> name
    }
    
    # Load fields mappings
    fields_path = 'docs/fields.csv'
    if os.path.exists(fields_path):
        with open(fields_path, 'r', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            for row in reader:
                mappings['fields'][row['searge']] = row['name']
    
    # Load methods mappings
    methods_path = 'docs/methods.csv'
    if os.path.exists(methods_path):
        with open(methods_path, 'r', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            for row in reader:
                mappings['methods'][row['searge']] = row['name']
    
    return mappings

def deobfuscate_file(file_path, mappings):
    """Deobfuscate a single Java file"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Replace method calls: func_xxxx_xx() -> methodName()
    for searge, name in mappings['methods'].items():
        # Pattern for method calls: func_xxxx_xx() or func_xxxx_xx(param1, param2, ...)
        pattern = re.compile(rf'\b{searge}\s*\(')
        content = pattern.sub(f'{name}(', content)
    
    # Replace field access: field_xxxx_xx -> fieldName
    for searge, name in mappings['fields'].items():
        # Pattern for field access: field_xxxx_xx
        pattern = re.compile(rf'\b{searge}\b')
        content = pattern.sub(name, content)
    
    # Write back the deobfuscated content
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)
    
    return True

def main():
    """Main function"""
    print("Loading mappings...")
    mappings = load_mappings()
    
    print(f"Loaded {len(mappings['fields'])} field mappings")
    print(f"Loaded {len(mappings['methods'])} method mappings")
    
    # Find all Java files in src directory
    src_dir = 'src'
    java_files = []
    
    for root, dirs, files in os.walk(src_dir):
        for file in files:
            if file.endswith('.java'):
                java_files.append(os.path.join(root, file))
    
    print(f"Found {len(java_files)} Java files to process")
    
    # Process each Java file
    processed = 0
    for file_path in java_files:
        print(f"Processing {file_path}...")
        if deobfuscate_file(file_path, mappings):
            processed += 1
    
    print(f"\nDeobfuscation complete!")
    print(f"Processed {processed} files")

if __name__ == "__main__":
    main()
